package cloud.chlora.management.iot.cluster.dto.response;

import cloud.chlora.management.iot.device.dto.response.DeviceListResponse;

import java.util.List;

public record ClusterGetResponse(
        String clusterId,
        String clusterName,
        long totalDevices,
        List<DeviceListResponse> devices
) {}
