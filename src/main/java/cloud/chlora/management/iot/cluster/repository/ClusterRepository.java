package cloud.chlora.management.iot.cluster.repository;

import cloud.chlora.management.iot.cluster.domain.Cluster;
import cloud.chlora.management.iot.cluster.dto.query.ClusterQuery;
import cloud.chlora.management.iot.cluster.dto.response.ClusterListResponse;
import cloud.chlora.management.common.enums.IotErrorCode;
import cloud.chlora.management.common.exception.AppException;
import cloud.chlora.management.iot.device.domain.Device;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ClusterRepository {

    private final JdbcClient jdbcClient;

    public List<ClusterListResponse> findWithDeviceCount(ClusterQuery clusterQuery) {
        String sql = """
            SELECT c.cluster_id, c.cluster_name, COUNT(d.device_id) AS total_devices
            FROM clusters c
            LEFT JOIN devices d
                ON d.cluster_id = c.cluster_id
                AND d.deleted_at IS NULL
            WHERE c.deleted_at IS NULL
              AND (
                  :search::text IS NULL
                  OR c.cluster_id ILIKE '%' || :search || '%'
                  OR c.cluster_name ILIKE '%' || :search || '%'
              )
            GROUP BY c.cluster_id, c.cluster_name, c.created_at
            ORDER BY
                CASE WHEN :sort = 'clusterId' AND :order = 'asc' THEN c.cluster_id END ,
                CASE WHEN :sort = 'clusterId' AND :order = 'desc' THEN c.cluster_id END DESC,

                CASE WHEN :sort = 'clusterName' AND :order = 'asc' THEN c.cluster_name END ,
                CASE WHEN :sort = 'clusterName' AND :order = 'desc' THEN c.cluster_name END DESC,

                CASE WHEN :sort = 'createdAt' AND :order = 'asc' THEN c.created_at END ,
                CASE WHEN :sort = 'createdAt' AND :order = 'desc' THEN c.created_at END DESC,

                CASE WHEN :sort = 'totalDevices' AND :order = 'asc' THEN COUNT(d.device_id) END ,
                CASE WHEN :sort = 'totalDevices' AND :order = 'desc' THEN COUNT(d.device_id) END DESC
            LIMIT :limit OFFSET :offset
            """;

        int offset = (clusterQuery.getPage() - 1) * clusterQuery.getSize();

        return jdbcClient.sql(sql)
                .param("search", normalizeSearch(clusterQuery.getSearch()))
                .param("sort", normalizeSort(clusterQuery.getSort()))
                .param("order", normalizeOrder(clusterQuery.getOrder()))
                .param("limit", clusterQuery.getSize())
                .param("offset", offset)
                .query((rs, rowNum) -> new ClusterListResponse(
                        rs.getString("cluster_id"),
                        rs.getString("cluster_name"),
                        rs.getLong("total_devices")
                ))
                .list();
    }

    public long countExistingClusters(ClusterQuery clusterQuery) {
        String sql = """
        SELECT COUNT(*)
        FROM clusters c
        WHERE c.deleted_at IS NULL
          AND (
              :search::text IS NULL
              OR c.cluster_id ILIKE '%' || :search || '%'
              OR c.cluster_name ILIKE '%' || :search || '%'
          )
    """;

        return jdbcClient.sql(sql)
                .param("search", normalizeSearch(clusterQuery.getSearch()))
                .query(Long.class)
                .single();
    }

    public Optional<Cluster> findByClusterId(String clusterId) {
        String sql = "SELECT * FROM clusters WHERE cluster_id = ?";
        return jdbcClient.sql(sql)
                .param(clusterId)
                .query(Cluster.class)
                .optional();
    }

    public Cluster save(Cluster cluster) {
        String sql = """
        INSERT INTO clusters (cluster_name)
        VALUES (:clusterName)
        RETURNING id, cluster_id, cluster_name, created_at, updated_at, deleted_at
        """;

        return jdbcClient.sql(sql)
                .param("clusterName", cluster.getClusterName())
                .query((rs, rowNum) -> Cluster.builder()
                        .id(rs.getLong("id"))
                        .clusterId(rs.getString("cluster_id"))
                        .clusterName(rs.getString("cluster_name"))
                        .createdAt(rs.getTimestamp("created_at").toInstant())
                        .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
                        .deletedAt(rs.getTimestamp("deleted_at") != null ? rs.getTimestamp("deleted_at").toInstant() : null)
                        .build())
                .single();
    }

    public void softDelete(String clusterId) {
        String sql = """
                UPDATE clusters
                SET deleted_at = NOW()
                WHERE cluster_id = ? AND deleted_at IS NULL
                """;

        jdbcClient.sql(sql)
                .param(clusterId)
                .update();
    }

    public long countDevice(String clusterId) {
        String sql = "SELECT COUNT(*) FROM devices WHERE cluster_id = ? AND deleted_at IS NULL";

        return jdbcClient.sql(sql)
                .param(clusterId)
                .query(Long.class)
                .single();
    }

    public List<Device> findAllDevicesByClusterId(String clusterId) {
        String sql = "SELECT * FROM devices WHERE cluster_id = ?AND deleted_at IS NULL";

        return jdbcClient.sql(sql)
                .param(clusterId)
                .query(Device.class)
                .list();
    }

    public boolean isClusterNameExists(String clusterName) {
        String sql = "SELECT COUNT(*) FROM clusters WHERE cluster_name = ?";
        return jdbcClient.sql(sql)
                .param(clusterName)
                .query(Long.class)
                .single() > 0;
    }

    public Cluster update(Cluster cluster) {
        String sql = """
                UPDATE clusters
                SET cluster_name = ?, updated_at  = ?
                WHERE cluster_id = ? AND deleted_at IS NULL
                """;

        int updatedRows = jdbcClient.sql(sql)
                .param(cluster.getClusterName())
                .param(Timestamp.from(cluster.getUpdatedAt()))
                .param(cluster.getClusterId())
                .update();

        if (updatedRows == 0) {
            throw AppException.of(IotErrorCode.CLUSTER_NOT_FOUND);
        }

        return findByClusterId(cluster.getClusterId())
                .orElseThrow(() -> AppException.of(IotErrorCode.CLUSTER_NOT_FOUND));
    }

    public void softDeleteDevicesByClusterId(String clusterId) {
        String sql = """
                UPDATE devices
                SET deleted_at = NOW()
                WHERE cluster_id = ? AND deleted_at IS NULL
                """;

        jdbcClient.sql(sql)
                .param(clusterId)
                .update();
    }

    public boolean isClusterIdExists(String clusterId) {
        String sql = "SELECT COUNT(*) FROM clusters WHERE cluster_id = ?";
        return jdbcClient.sql(sql)
                .param(clusterId)
                .query(Long.class)
                .single() > 0;
    }

    // ===== HELPER ===== //
    private String normalizeSearch(String search) {
        if (search == null) {
            return null;
        }

        String trimmed = search.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizeSort(String sort) {
        return switch (sort) {
            case "clusterId", "cluster_id" -> "clusterId";
            case "clusterName", "cluster_name" -> "clusterName";
            case "createdAt", "created_at" -> "createdAt";
            case "totalDevices", "total_devices" -> "totalDevices";
            default -> "createdAt";
        };
    }

    private String normalizeOrder(String order) {
        return "asc".equalsIgnoreCase(order) ? "asc" : "desc";
    }
}
