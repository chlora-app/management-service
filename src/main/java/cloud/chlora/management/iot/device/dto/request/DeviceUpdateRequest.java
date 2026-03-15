package cloud.chlora.management.iot.device.dto.request;

public record DeviceUpdateRequest(
        String deviceName,
        String deviceType,
        String status,
        String clusterId
) {}