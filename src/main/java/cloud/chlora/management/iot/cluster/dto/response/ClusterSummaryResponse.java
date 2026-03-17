package cloud.chlora.management.iot.cluster.dto.response;

public record ClusterSummaryResponse(
        String clusterId,
        String clusterName,
        long totalDevices
) {}
