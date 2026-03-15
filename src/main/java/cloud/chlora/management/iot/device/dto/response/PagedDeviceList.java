package cloud.chlora.management.iot.device.dto.response;

import cloud.chlora.management.iot.device.domain.DeviceStatus;
import lombok.Builder;

import java.time.Instant;

@Builder
public record PagedDeviceList(
        String deviceId,
        String deviceName,
        String deviceType,
        DeviceStatus status,
        String clusterId,
        String clusterName,
        Instant createdAt
) {}
