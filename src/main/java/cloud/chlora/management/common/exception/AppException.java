package cloud.chlora.management.common.exception;

import cloud.chlora.management.common.enums.AppErrorCode;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

    private final AppErrorCode errorCode;

    private AppException(AppErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    public static AppException of(AppErrorCode error) {
        return new AppException(error);
    }

}