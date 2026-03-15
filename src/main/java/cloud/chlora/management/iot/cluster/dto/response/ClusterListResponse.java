package cloud.chlora.management.iot.cluster.dto.response;

public record ClusterListResponse(
        String clusterId,
        String clusterName,
        long totalDevices
) {}
