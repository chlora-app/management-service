package cloud.chlora.management.iot.cluster.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClusterListResponse(
        List<ClusterInfo> list
) {
    public record ClusterInfo(
            @JsonProperty("label")
            String clusterId,

            @JsonProperty("value")
            String clusterName
    ) {}
}
