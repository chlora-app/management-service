package cloud.chlora.management.iot.device.controller;

import cloud.chlora.management.iot.device.dto.query.DeviceQuery;
import cloud.chlora.management.iot.device.dto.request.DeviceCreateRequest;
import cloud.chlora.management.iot.device.dto.request.DeviceUpdateRequest;
import cloud.chlora.management.iot.device.dto.response.DeviceCreateResponse;
import cloud.chlora.management.iot.device.dto.response.DeviceGetResponse;
import cloud.chlora.management.iot.device.dto.response.DeviceUpdateResponse;
import cloud.chlora.management.iot.device.dto.response.PagedDeviceResponse;
import cloud.chlora.management.iot.device.service.DeviceService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping("/health")
    public String health() {
        return "OK";
    }

    @GetMapping
    public ResponseEntity<@NonNull PagedDeviceResponse> findAllExistingDevices(@ModelAttribute DeviceQuery query) {
        PagedDeviceResponse response = deviceService.findAllExistingDevices(query);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<@NonNull DeviceCreateResponse> createDevice(@RequestBody DeviceCreateRequest request) {
        DeviceCreateResponse response = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{deviceId}")
    public ResponseEntity<@NonNull DeviceGetResponse> findByDeviceId(@PathVariable("deviceId") String deviceId) {
        DeviceGetResponse response = deviceService.findByDeviceId(deviceId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{deviceId}")
    public ResponseEntity<@NonNull DeviceUpdateResponse> updateDevice(
            @PathVariable("deviceId") String deviceId,
            @RequestBody DeviceUpdateRequest request
    ) {
        DeviceUpdateResponse response = deviceService.updateDevice(deviceId, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{deviceId}")
    public ResponseEntity<@NonNull Void> deleteDevice(@PathVariable("deviceId") String deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResponseEntity.noContent().build();
    }
}
