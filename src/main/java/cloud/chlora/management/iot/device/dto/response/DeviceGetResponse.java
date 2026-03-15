package cloud.chlora.management.iot.device.dto.response;

import cloud.chlora.management.iot.device.domain.DeviceStatus;

import java.time.Instant;

public record DeviceGetResponse(
        String deviceId,
        String deviceName,
        String deviceType,
        String clusterId,
        DeviceStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
