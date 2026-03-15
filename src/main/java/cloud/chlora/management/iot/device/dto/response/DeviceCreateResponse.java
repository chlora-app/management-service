package cloud.chlora.management.iot.device.dto.response;

import cloud.chlora.management.iot.device.domain.DeviceStatus;

import java.time.Instant;

public record DeviceCreateResponse(
        String deviceId,
        String deviceName,
        String deviceType,
        String clusterId,
        DeviceStatus status,
        Instant createdAt
) {}
