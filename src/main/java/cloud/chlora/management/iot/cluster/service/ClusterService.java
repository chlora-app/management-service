package cloud.chlora.management.iot.cluster.service;

import cloud.chlora.management.common.mapper.ResponseMapper;
import cloud.chlora.management.iot.cluster.domain.Cluster;
import cloud.chlora.management.iot.cluster.dto.query.ClusterQuery;
import cloud.chlora.management.iot.cluster.dto.request.ClusterCreateRequest;
import cloud.chlora.management.iot.cluster.dto.request.ClusterUpdateRequest;
import cloud.chlora.management.iot.cluster.dto.response.*;
import cloud.chlora.management.iot.cluster.repository.ClusterRepository;
import cloud.chlora.management.common.enums.IotErrorCode;
import cloud.chlora.management.common.exception.AppException;
import cloud.chlora.management.common.helper.LogHelper;
import cloud.chlora.management.iot.device.dto.response.DeviceListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClusterService {

    private final ClusterRepository clusterRepository;

    public PagedClusterResponse findAllExistingClusters(ClusterQuery query) {
        if (query.getPage() < 1) {
            throw AppException.of(IotErrorCode.PAGE_LOWER_THAN_ONE);
        }

        if (query.getSize() < 1) {
            throw AppException.of(IotErrorCode.SIZE_LOWER_THAN_ONE);
        }

        List<ClusterListResponse> clusterList = clusterRepository.findWithDeviceCount(query);
        long totalData = clusterRepository.countExistingClusters(query);
        int totalPages = (int) Math.ceil((double) totalData / query.getSize());

        return ResponseMapper.ClusterMapper.toPagedResponse(
                totalData,
                query.getPage(),
                query.getSize(),
                totalPages,
                clusterList
        );
    }

    @Transactional
    public ClusterCreateResponse createCluster(ClusterCreateRequest request) {
        Cluster cluster = Cluster.builder()
                .clusterName(request.clusterName())
                .build();

        Cluster savedCluster = clusterRepository.save(cluster);
        LogHelper.Cluster.success(log, "Cluster created successfully", "createCluster", savedCluster.getClusterId());

        return ResponseMapper.ClusterMapper.toCreateResponse(savedCluster);
    }

    public ClusterGetResponse findByClusterId(String clusterId) {
        Cluster cluster = clusterRepository.findByClusterId(clusterId)
                .orElseThrow(() -> {
                    LogHelper.Cluster.error(log, IotErrorCode.CLUSTER_NOT_FOUND, "findByClusterId", clusterId);
                    return AppException.of(IotErrorCode.CLUSTER_NOT_FOUND);
                });

        if (cluster.getDeletedAt() != null) {
            LogHelper.Cluster.error(log, IotErrorCode.CLUSTER_ALREADY_DELETED, "findByClusterId", clusterId);
            throw AppException.of(IotErrorCode.CLUSTER_ALREADY_DELETED);
        }

        long deviceCount = clusterRepository.countDevice(clusterId);

        List<DeviceListResponse> deviceList =
                clusterRepository.findAllDevicesByClusterId(clusterId)
                        .stream()
                        .map(ResponseMapper.DeviceMapper::toListResponse)
                        .toList();

        return ResponseMapper.ClusterMapper.toGetResponse(
                cluster,
                deviceCount,
                deviceList
        );
    }

    @Transactional
    public ClusterUpdateResponse updateCluster(String clusterId, ClusterUpdateRequest request) {
        Cluster cluster = clusterRepository.findByClusterId(clusterId)
                .orElseThrow(() -> {
                    LogHelper.Cluster.error(log, IotErrorCode.CLUSTER_NOT_FOUND, "updateCluster", clusterId);
                    return AppException.of(IotErrorCode.CLUSTER_NOT_FOUND);
                });

        boolean isUpdated = false;

        if (request.clusterName() != null) {
            if (clusterRepository.isClusterNameExists(request.clusterName())) {
                LogHelper.Cluster.error(log, IotErrorCode.CLUSTER_NAME_ALREADY_EXISTS, "updateCluster", clusterId);
                throw AppException.of(IotErrorCode.CLUSTER_NAME_ALREADY_EXISTS);
            }

            cluster.setClusterName(request.clusterName());
            isUpdated = true;
        }

        if (!isUpdated) {
            LogHelper.Cluster.error(log, IotErrorCode.CLUSTER_UPDATE_EMPTY, "updateCluster", clusterId);
            throw AppException.of(IotErrorCode.CLUSTER_UPDATE_EMPTY);
        }

        cluster.setUpdatedAt(Instant.now());

        Cluster updatedCluster = clusterRepository.update(cluster);
        LogHelper.Cluster.success(log, "Cluster updated successfully", "updateCluster", clusterId);

        return ResponseMapper.ClusterMapper.toUpdateResponse(updatedCluster);
    }

    @Transactional
    public void deleteCluster(String clusterId) {
        Cluster cluster = clusterRepository.findByClusterId(clusterId)
                .orElseThrow(() -> {
                    LogHelper.Cluster.error(log, IotErrorCode.CLUSTER_NOT_FOUND, "deleteCluster", clusterId);
                    return AppException.of(IotErrorCode.CLUSTER_NOT_FOUND);
                });

        if (cluster.getDeletedAt() != null) {
            LogHelper.Cluster.error(log, IotErrorCode.CLUSTER_ALREADY_DELETED, "deleteCluster", clusterId);
            throw AppException.of(IotErrorCode.CLUSTER_ALREADY_DELETED);
        }

        clusterRepository.softDelete(clusterId);
        clusterRepository.softDeleteDevicesByClusterId(clusterId);
        LogHelper.Cluster.success(log, "Cluster deleted successfully", "deleteCluster", clusterId);
    }
}
