package cloud.chlora.management.iot.cluster.dto.response;

import java.util.List;

public record PagedClusterResponse(
        long countData,
        int page,
        int size,
        int totalPages,
        List<ClusterSummaryResponse> clusters
) {}
