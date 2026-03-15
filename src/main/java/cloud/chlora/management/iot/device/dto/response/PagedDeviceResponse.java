package cloud.chlora.management.iot.device.dto.response;

import java.util.List;

public record PagedDeviceResponse(
        long countData,
        int page,
        int size,
        int totalPages,
        List<PagedDeviceList> devices
) {}
