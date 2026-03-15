package cloud.chlora.management.common.error;

import org.springframework.http.HttpStatus;

public sealed interface AppError permits AppError.NotFound, AppError.Conflict, AppError.BadRequest, AppError.Internal {

    String code();
    String message();
    HttpStatus status();

    record NotFound(String code, String message) implements AppError {

        @Override
        public HttpStatus status() {
            return HttpStatus.NOT_FOUND;
        }
    }

    record Conflict(String code, String message) implements AppError {

        @Override
        public HttpStatus status() {
            return HttpStatus.CONFLICT;
        }
    }

    record BadRequest(String code, String message) implements AppError {

        @Override
        public HttpStatus status() {
            return HttpStatus.BAD_REQUEST;
        }
    }

    record Internal(String code, String message) implements AppError {

        @Override
        public HttpStatus status() {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}