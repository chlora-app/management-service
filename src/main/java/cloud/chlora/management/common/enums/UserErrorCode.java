package cloud.chlora.management.common.enums;

import org.springframework.http.HttpStatus;

public enum UserErrorCode implements AppErrorCode {

    USER_PATCH_EMPTY("AUTH-400-001", HttpStatus.BAD_REQUEST, "user", "No fields provided to patch"),

    USER_NOT_FOUND("USER-404-001", HttpStatus.NOT_FOUND, "user","User not found"),

    USER_IS_ACTIVE("USER-409-001", HttpStatus.CONFLICT, "user","User is active"),
    EMAIL_ALREADY_EXISTS("AUTH-409-002", HttpStatus.CONFLICT, "user","Email already exists"),
    USER_ALREADY_DELETED("USER-409-003", HttpStatus.CONFLICT, "user","User already deleted"),

    USER_RESTORE_FAILED("USER-500-001", HttpStatus.INTERNAL_SERVER_ERROR, "user","User restore failed"),
    USER_DELETE_FAILED("USER-500-002", HttpStatus.INTERNAL_SERVER_ERROR, "user","User delete failed");

    private final String code;
    private final HttpStatus status;
    private final String domain;
    private final String message;

    UserErrorCode(String code, HttpStatus status, String domain, String message) {
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
