package cloud.chlora.management.iot.device.dto.response;

import cloud.chlora.management.iot.device.domain.DeviceStatus;

import java.time.Instant;

public record DeviceUpdateResponse(
        String deviceId,
        String deviceName,
        String deviceType,
        DeviceStatus status,
        String clusterId,
        Instant updatedAt
) {}
