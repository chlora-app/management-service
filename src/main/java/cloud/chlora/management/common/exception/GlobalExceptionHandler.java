package cloud.chlora.management.common.exception;

import cloud.chlora.management.common.enums.AppErrorCode;
import cloud.chlora.management.common.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleAppException(AppException ex, HttpServletRequest request) {
        AppErrorCode error = ex.getErrorCode();
        String path = "%s %s".formatted(request.getMethod(), request.getRequestURI());

        return ResponseEntity
                .status(error.status())
                .body(new ErrorResponse(
                        error.code(),
                        error.message(),
                        error.status().value(),
                        path,
                        MDC.get("traceId"),
                        Instant.now().toString()
                ));
    }
}