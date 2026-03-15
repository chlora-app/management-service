package cloud.chlora.management.common.enums;

import org.springframework.http.HttpStatus;

public enum IotErrorCode implements AppErrorCode {

    DEVICE_STATUS_INVALID("DVC-400-001", HttpStatus.BAD_REQUEST, "device", "Invalid device status"),
    DEVICE_UPDATE_EMPTY("DVC-400-002", HttpStatus.BAD_REQUEST, "device", "Device update request is empty"),
    DEVICE_NOT_FOUND("DVC-404-001", HttpStatus.NOT_FOUND, "device", "Device not found"),
    DEVICE_NOT_FOUND_AFTER_UPDATE("DVC-404-002", HttpStatus.NOT_FOUND, "device", "Device not found after update"),
    DEVICE_UPDATE_FAILED("DVC-409-001", HttpStatus.CONFLICT, "device", "Device update failed"),
    DEVICE_ALREADY_DELETED("DVC-409-002", HttpStatus.CONFLICT, "device", "Device already deleted"),

    CLUSTER_UPDATE_EMPTY("CLS-400-001", HttpStatus.BAD_REQUEST, "cluster", "Cluster update request is empty"),
    CLUSTER_NOT_FOUND("CLS-404-001", HttpStatus.NOT_FOUND, "cluster", "Cluster not found"),
    CLUSTER_NAME_ALREADY_EXISTS("CLS-409-001", HttpStatus.CONFLICT, "cluster", "Cluster name already exists"),
    CLUSTER_ALREADY_DELETED("CLS-409-002", HttpStatus.CONFLICT, "cluster", "Cluster already deleted"),

    PAGE_LOWER_THAN_ONE("GEN-400-001", HttpStatus.BAD_REQUEST, "pagination", "Page must be greater than 0"),
    SIZE_LOWER_THAN_ONE("GEN-400-002", HttpStatus.BAD_REQUEST, "pagination", "Size must be greater than 0");

    private final String code;
    private final HttpStatus status;
    private final String domain;
    private final String message;

    IotErrorCode(String code, HttpStatus status, String domain, String message) {
        this.code = code;
        this.status = status;
        this.domain = domain;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    public String domain() {
        return domain;
    }
}