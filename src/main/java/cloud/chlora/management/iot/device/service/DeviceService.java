package cloud.chlora.management.iot.device.service;

import cloud.chlora.management.common.enums.IotErrorCode;
import cloud.chlora.management.common.exception.AppException;
import cloud.chlora.management.common.helper.LogHelper;
import cloud.chlora.management.common.mapper.ResponseMapper;
import cloud.chlora.management.iot.cluster.repository.ClusterRepository;
import cloud.chlora.management.iot.device.domain.Device;
import cloud.chlora.management.iot.device.domain.DeviceStatus;
import cloud.chlora.management.iot.device.dto.query.DeviceQuery;
import cloud.chlora.management.iot.device.dto.request.DeviceCreateRequest;
import cloud.chlora.management.iot.device.dto.request.DeviceUpdateRequest;
import cloud.chlora.management.iot.device.dto.response.*;
import cloud.chlora.management.iot.device.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final ClusterRepository clusterRepository;

    public PagedDeviceResponse findAllExistingDevices(DeviceQuery query) {
        if (query.getPage() < 1) {
            throw AppException.of(IotErrorCode.PAGE_LOWER_THAN_ONE);
        }

        if (query.getSize() < 1) {
            throw AppException.of(IotErrorCode.SIZE_LOWER_THAN_ONE);
        }

        long totalData = deviceRepository.countExistingDevices(query);
        List<PagedDeviceList> devices = deviceRepository.findAllDevices(query);

        int totalPages = (int) Math.ceil((double) totalData / query.getSize());

        return ResponseMapper.DeviceMapper.toPagedResponse(
                totalData,
                query.getPage(),
                query.getSize(),
                totalPages,
                devices
        );
    }

    @Transactional
    public DeviceCreateResponse createDevice(DeviceCreateRequest request) {
        if (request.deviceName().isBlank()) {
            LogHelper.Device.error(log, IotErrorCode.DEVICE_REQUEST_INVALID, "createDevice", null);
            throw AppException.of(IotErrorCode.DEVICE_REQUEST_INVALID);
        }

        if (!clusterRepository.isClusterIdExists(request.clusterId())) {
            LogHelper.Device.notFound(log, IotErrorCode.CLUSTER_NOT_FOUND, "createDevice", request.clusterId());
            throw AppException.of(IotErrorCode.CLUSTER_NOT_FOUND);
        }

        Device device = Device.builder()
                .deviceName(request.deviceName())
                .deviceType(request.deviceType())
                .status(DeviceStatus.OFFLINE)
                .clusterId(request.clusterId())
                .build();

        Device savedDevice = deviceRepository.save(device);
        LogHelper.Device.success(log, "Device created successfully", "createDevice", savedDevice.getDeviceId());

        return ResponseMapper.DeviceMapper.toCreateResponse(savedDevice);
    }

    public DeviceGetResponse findByDeviceId(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> {
                    LogHelper.Device.notFound(log, IotErrorCode.DEVICE_NOT_FOUND, "findByDeviceId", deviceId);
                    return AppException.of(IotErrorCode.DEVICE_NOT_FOUND);
                });

        if (device.getDeletedAt() != null) {
            LogHelper.Device.conflict(log, IotErrorCode.DEVICE_ALREADY_DELETED, "findByDeviceId", deviceId);
            throw AppException.of(IotErrorCode.DEVICE_ALREADY_DELETED);
        }

        return ResponseMapper.DeviceMapper.toGetResponse(device);
    }

    @Transactional
    public DeviceUpdateResponse updateDevice(String deviceId, DeviceUpdateRequest request) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> {
                    LogHelper.Device.notFound(log, IotErrorCode.DEVICE_NOT_FOUND, "updateDevice", deviceId);
                    return AppException.of(IotErrorCode.DEVICE_NOT_FOUND);
                });

        if (device.getDeletedAt() != null) {
            LogHelper.Device.conflict(log, IotErrorCode.DEVICE_ALREADY_DELETED, "updateDevice", deviceId);
            throw AppException.of(IotErrorCode.DEVICE_ALREADY_DELETED);
        }

        boolean isUpdated = false;

        if (request.deviceName() != null) {
            if (request.deviceName().isBlank()) {
                LogHelper.Device.error(log, IotErrorCode.DEVICE_REQUEST_INVALID, "updateDevice", null);
                throw AppException.of(IotErrorCode.DEVICE_REQUEST_INVALID);
            }

            device.setDeviceName(request.deviceName());
            isUpdated = true;
        }

        if (request.deviceType() != null) {
            device.setDeviceType(request.deviceType());
            isUpdated = true;
        }

        if (request.status() != null) {
            device.setStatus(normalizeStatus(request.status()));
            isUpdated = true;
        }

        if (request.clusterId() != null) {
            if (!clusterRepository.isClusterIdExists(request.clusterId())) {
                LogHelper.Device.notFound(log, IotErrorCode.CLUSTER_NOT_FOUND, "updateDevice", deviceId);
                throw AppException.of(IotErrorCode.CLUSTER_NOT_FOUND);
            }

            device.setClusterId(request.clusterId());
            isUpdated = true;
        }

        if (!isUpdated) {
            LogHelper.Device.error(log, IotErrorCode.DEVICE_UPDATE_EMPTY, "updateDevice", deviceId);
            throw AppException.of(IotErrorCode.DEVICE_UPDATE_EMPTY);
        }

        device.setUpdatedAt(Instant.now());

        Device updatedDevice = deviceRepository.update(device);
        LogHelper.Device.success(log, "Device updated successfully", "updateDevice", deviceId);

        return ResponseMapper.DeviceMapper.toUpdateResponse(updatedDevice);
    }

    @Transactional
    public void deleteDevice(String deviceId) {
        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> {
                    LogHelper.Device.notFound(log, IotErrorCode.DEVICE_NOT_FOUND, "deleteDevice", deviceId);
                    return AppException.of(IotErrorCode.DEVICE_NOT_FOUND);
                });

        if (device.getDeletedAt() != null) {
            LogHelper.Device.conflict(log, IotErrorCode.DEVICE_ALREADY_DELETED, "deleteDevice", deviceId);
            throw AppException.of(IotErrorCode.DEVICE_ALREADY_DELETED);
        }

        deviceRepository.softDelete(deviceId);
        LogHelper.Device.success(log, "Device deleted successfully", "deleteDevice", deviceId);
    }

    // =========================
    // Helper
    // =========================
    private DeviceStatus normalizeStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }

        try {
            return DeviceStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            LogHelper.Device.error(log, IotErrorCode.DEVICE_STATUS_INVALID, "normalizeStatus", null);
            throw AppException.of(IotErrorCode.DEVICE_STATUS_INVALID);
        }
    }
}
