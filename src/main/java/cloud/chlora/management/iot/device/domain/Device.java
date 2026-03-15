package cloud.chlora.management.iot.device.domain;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class Device {

    private Long id;

    private String deviceId;
    private String deviceName;
    private String deviceType;
    private DeviceStatus status;

    private String clusterId;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}