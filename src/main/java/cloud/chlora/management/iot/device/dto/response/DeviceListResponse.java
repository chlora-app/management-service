package cloud.chlora.management.iot.device.dto.response;

import cloud.chlora.management.iot.device.domain.DeviceStatus;

public record DeviceListResponse(
        String deviceId,
        String deviceName,
        String deviceType,
        DeviceStatus status
) {}
