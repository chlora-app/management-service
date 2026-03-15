package cloud.chlora.management.iot.device.repository;

import cloud.chlora.management.common.enums.IotErrorCode;
import cloud.chlora.management.common.exception.AppException;
import cloud.chlora.management.iot.device.domain.Device;
import cloud.chlora.management.iot.device.domain.DeviceStatus;
import cloud.chlora.management.iot.device.dto.query.DeviceQuery;
import cloud.chlora.management.iot.device.dto.response.PagedDeviceList;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeviceRepository {

    private final JdbcClient jdbcClient;

    public Optional<Device> findByDeviceId(String deviceId) {
        String sql = "SELECT * FROM devices WHERE device_id = ?";
        return jdbcClient.sql(sql)
                .param(deviceId)
                .query(Device.class)
                .optional();
    }

    public Device save(Device device) {
        String sql = """
                INSERT INTO devices (device_name, device_type, cluster_id, status)
                VALUES (:deviceName, :deviceType, :clusterId, :status)
                RETURNING *
                """;

        return jdbcClient.sql(sql)
                .param("deviceName", device.getDeviceName())
                .param("deviceType", device.getDeviceType())
                .param("clusterId", device.getClusterId())
                .param("status", device.getStatus().toString())
                .query(Device.class)
                .single();
    }

    public Device update(Device device) {
        String sql = """
                UPDATE devices
                SET device_name = :deviceName, device_type = :deviceType, status = :status, cluster_id = :clusterId, updated_at = :updatedAt
                WHERE device_id = :deviceId AND deleted_at IS NULL
                """;

        int updatedRows = jdbcClient.sql(sql)
                .param("deviceName", device.getDeviceName())
                .param("deviceType", device.getDeviceType())
                .param("status", device.getStatus().name())
                .param("clusterId", device.getClusterId())
                .param("updatedAt", Timestamp.from(device.getUpdatedAt()))
                .param("deviceId", device.getDeviceId())
                .update();

        if (updatedRows == 0) {
            throw AppException.of(IotErrorCode.DEVICE_UPDATE_FAILED);
        }

        return findByDeviceId(device.getDeviceId())
                .orElseThrow(() -> AppException.of(IotErrorCode.DEVICE_NOT_FOUND_AFTER_UPDATE));
    }

    public void softDelete(String deviceId) {
        String sql = "UPDATE devices SET deleted_at = NOW() WHERE device_id = ? AND deleted_at IS NULL";

        int affected = jdbcClient.sql(sql)
                .param(deviceId)
                .update();

        if (affected == 0) {
            throw AppException.of(IotErrorCode.DEVICE_NOT_FOUND);
        }
    }

    public List<PagedDeviceList> findAllDevices(DeviceQuery query) {
        String sql = """
                SELECT
                    d.device_id, d.device_name, d.device_type, d.status, c.cluster_id,
                    c.cluster_name, d.created_at, d.updated_at, d.deleted_at
                FROM devices d
                JOIN clusters c ON d.cluster_id = c.cluster_id
                WHERE d.deleted_at IS NULL
                    AND (
                        :search::text IS NULL
                        OR d.device_id ILIKE '%' || :search || '%'
                        OR d.device_name ILIKE '%' || :search || '%'
                        OR d.device_type ILIKE '%' || :search || '%'
                        OR d.status ILIKE '%' || :search || '%'
                        OR d.cluster_id ILIKE '%' || :search || '%'
                        OR c.cluster_name ILIKE '%' || :search || '%'
                    )
                    AND (
                        :clusterId::text IS NULL
                        OR d.cluster_id = :clusterId
                    )
                    AND (
                        :status::text IS NULL
                        OR d.status = :status
                    )
                ORDER BY
                    CASE WHEN :sort = 'deviceId' AND :order = 'asc'  THEN d.device_id END,
                    CASE WHEN :sort = 'deviceId' AND :order = 'desc' THEN d.device_id END DESC,
                
                    CASE WHEN :sort = 'deviceName' AND :order = 'asc'  THEN d.device_name END,
                    CASE WHEN :sort = 'deviceName' AND :order = 'desc' THEN d.device_name END DESC,
                
                    CASE WHEN :sort = 'deviceType' AND :order = 'asc'  THEN d.device_type END,
                    CASE WHEN :sort = 'deviceType' AND :order = 'desc' THEN d.device_type END DESC,
                
                    CASE WHEN :sort = 'status' AND :order = 'asc'  THEN d.status END,
                    CASE WHEN :sort = 'status' AND :order = 'desc' THEN d.status END DESC,
                
                    CASE WHEN :sort = 'clusterId' AND :order = 'asc'  THEN c.cluster_id END,
                    CASE WHEN :sort = 'clusterId' AND :order = 'asc'  THEN c.cluster_id END DESC,
                
                    CASE WHEN :sort = 'clusterName' AND :order = 'asc'  THEN c.cluster_name END,
                    CASE WHEN :sort = 'clusterName' AND :order = 'desc' THEN c.cluster_name END DESC
                LIMIT :limit OFFSET :offset
                """;

        return jdbcClient.sql(sql)
                .param("search", normalizeSearch(query.getSearch()))
                .param("clusterId", normalizeSearch(query.getClusterId()))
                .param("status", normalizeStatus(query.getStatus()))
                .param("sort", normalizeSort(query.getSort()))
                .param("order", normalizeOrder(query.getOrder()))
                .param("limit", query.getSize())
                .param("offset", (query.getPage() - 1) * query.getSize())
                .query(this::mapRow)
                .list();
    }

    public long countExistingDevices(DeviceQuery query) {
        String sql = """
                SELECT COUNT(*)
                FROM devices d
                JOIN clusters c ON c.cluster_id = d.cluster_id
                WHERE d.deleted_at IS NULL
                    AND (
                        :search::TEXT IS NULL
                        OR d.device_id ILIKE '%' || :search || '%'
                        OR d.device_name ILIKE '%' || :search || '%'
                        OR d.device_type ILIKE '%' || :search || '%'
                        OR d.status ILIKE '%' || :search || '%'
                        OR d.cluster_id ILIKE '%' || :search || '%'
                        OR c.cluster_name ILIKE '%' || :search || '%'
                    )
                    AND (
                        :clusterId::text IS NULL
                        OR d.cluster_id = :clusterId
                    )
                    AND (
                        :status::text IS NULL
                        OR d.status = :status
                    )
                """;

        return jdbcClient.sql(sql)
                .param("search", normalizeSearch(query.getSearch()))
                .param("clusterId", normalizeSearch(query.getClusterId()))
                .param("status", normalizeStatus(query.getStatus()))
                .query(Long.class)
                .single();
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
        if (sort == null) {
            return "createdAt";
        }

        return switch (sort) {
            case "deviceId", "device_id" -> "deviceId";
            case "deviceName", "device_name" -> "deviceName";
            case "deviceType", "device_type" -> "deviceType";
            case "status" -> "status";
            case "clusterId", "cluster_id" -> "clusterId";
            case "clusterName", "cluster_name" -> "clusterName";
            case "createdAt", "created_at" -> "createdAt";
            default -> "createdAt";
        };
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }

        String value = status.trim();
        if (value.isEmpty()) {
            return null;
        }

        try {
            return DeviceStatus.valueOf(value.toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw AppException.of(IotErrorCode.DEVICE_STATUS_INVALID);
        }
    }

    private String normalizeOrder(String order) {
        return "asc".equalsIgnoreCase(order) ? "asc" : "desc";
    }

    private PagedDeviceList mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PagedDeviceList.builder()
                .deviceId(rs.getString("device_id"))
                .deviceName(rs.getString("device_name"))
                .deviceType(rs.getString("device_type"))
                .status(DeviceStatus.valueOf(rs.getString("status")))
                .clusterId(rs.getString("cluster_id"))
                .clusterName(rs.getString("cluster_name"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .build();
    }
}
