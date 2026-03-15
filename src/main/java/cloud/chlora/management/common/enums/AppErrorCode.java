package cloud.chlora.management.common.enums;

import org.springframework.http.HttpStatus;

public interface AppErrorCode {

    String code();
    String message();
    HttpStatus status();
    String domain();
}