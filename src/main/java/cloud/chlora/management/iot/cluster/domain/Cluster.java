package cloud.chlora.management.iot.cluster.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Cluster {

    Long id;

    String clusterId;
    String clusterName;

    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt;
}
