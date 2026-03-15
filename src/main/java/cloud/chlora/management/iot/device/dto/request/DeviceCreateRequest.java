package cloud.chlora.management.iot.device.dto.request;

public record DeviceCreateRequest(
        String deviceName,
        String deviceType,
        String clusterId
) {}
