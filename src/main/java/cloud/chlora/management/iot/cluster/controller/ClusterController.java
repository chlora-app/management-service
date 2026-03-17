package cloud.chlora.management.iot.cluster.controller;

import cloud.chlora.management.iot.cluster.dto.query.ClusterQuery;
import cloud.chlora.management.iot.cluster.dto.request.ClusterCreateRequest;
import cloud.chlora.management.iot.cluster.dto.request.ClusterUpdateRequest;
import cloud.chlora.management.iot.cluster.dto.response.*;
import cloud.chlora.management.iot.cluster.service.ClusterService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clusters")
public class ClusterController {

    private final ClusterService clusterService;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping
    public ResponseEntity<@NonNull PagedClusterResponse> findAllExistingClusters(@ModelAttribute ClusterQuery query) {
        PagedClusterResponse response = clusterService.findAllExistingClusters(query);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<@NonNull ClusterCreateResponse> createCluster(@RequestBody ClusterCreateRequest request) {
        ClusterCreateResponse response = clusterService.createCluster(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<@NonNull ClusterGetResponse> findByClusterId(@PathVariable("clusterId") String clusterId) {
        ClusterGetResponse response = clusterService.findByClusterId(clusterId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{clusterId}")
    public ResponseEntity<@NonNull ClusterUpdateResponse> updateCluster(
            @PathVariable("clusterId") String clusterId,
            @RequestBody ClusterUpdateRequest request
    ) {
        ClusterUpdateResponse response = clusterService.updateCluster(clusterId, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{clusterId}")
    public ResponseEntity<@NonNull Void> deleteCluster(@PathVariable String clusterId) {
        clusterService.deleteCluster(clusterId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
    public ResponseEntity<@NonNull ClusterListResponse> getClusterList() {
        ClusterListResponse response = clusterService.getClusterList();
        return ResponseEntity.ok().body(response);
    }
}
