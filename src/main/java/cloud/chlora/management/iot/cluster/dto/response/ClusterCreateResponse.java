package cloud.chlora.management.iot.cluster.dto.response;

import java.time.Instant;

public record ClusterCreateResponse(
    String clusterId,
    String clusterName,
    Instant createdAt
) {}
