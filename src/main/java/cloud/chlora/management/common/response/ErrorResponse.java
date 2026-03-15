package cloud.chlora.management.common.response;

public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        String traceId,
        String timestamp
) {}
