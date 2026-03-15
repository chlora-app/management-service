package cloud.chlora.management.iot.device.dto.query;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeviceQuery {

    private int page = 1;
    private int size = 10;
    private String search;
    private String sort = "deviceId";
    private String order;
    private String status;
    private String clusterId;
}
