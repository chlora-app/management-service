package cloud.chlora.management.iot.cluster.dto.response;

import java.time.Instant;

public record ClusterUpdateResponse(
        String clusterId,
        String clusterName,
        Instant updatedAt
) {}
