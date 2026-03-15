package cloud.chlora.management.common.helper;

import cloud.chlora.management.common.enums.AppErrorCode;
import cloud.chlora.management.common.enums.IotErrorCode;
import cloud.chlora.management.common.enums.UserErrorCode;
import org.slf4j.Logger;
import org.slf4j.MDC;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

public final class LogHelper {

    private LogHelper() {}

    /*
     * =========================
     * Global Logging
     * =========================
     */
    public static void globalError(Logger logger, IotErrorCode error, String operation, String cause) {
        logger.error(
                error.message(),
                keyValue("error_code", error.code()),
                keyValue("error_key", error.name()),
                keyValue("domain", error.domain()),
                keyValue("operation", operation),
                keyValue("http_status", error.status().value()),
                keyValue("cause", cause),
                keyValue("result", "failed")
        );
    }

    public static class User {

        private static final String DOMAIN = "user";

        public static void success(Logger logger, String message, String operation, String userId) {
            logger.info(
                    message,
                    keyValue("domain", DOMAIN),
                    keyValue("operation", operation),
                    keyValue("user_id", userId),
                    keyValue("result", "success")
            );
        }

        public static void error(Logger logger, UserErrorCode error, String operation, String userId) {
            logError(logger, error, operation, "user_id", userId);
        }

        public static void notFound(Logger logger, UserErrorCode error, String operation, String userId) {
            logError(logger, error, operation, "user_id", userId);
        }

        public static void conflict(Logger logger, UserErrorCode error, String operation, String userId) {
            logError(logger, error, operation, "user_id", userId);
        }
    }

    /*
     * =========================
     * Device Logging
     * =========================
     */
    public static class Device {

        private static final String DOMAIN = "device";

        public static void success(Logger logger, String message, String operation, String deviceId) {
            logger.info(
                    message,
                    keyValue("domain", DOMAIN),
                    keyValue("operation", operation),
                    keyValue("device_id", deviceId),
                    keyValue("result", "success")
            );
        }

        public static void conflict(Logger logger, IotErrorCode error, String operation, String deviceId) {
            logError(logger, error, operation, "device_id", deviceId);
        }

        public static void error(Logger logger, IotErrorCode error, String operation, String deviceId) {
            logError(logger, error, operation, "device_id", deviceId);
        }

        public static void notFound(Logger logger, IotErrorCode error, String operation, String deviceId) {
            logError(logger, error, operation, "device_id", deviceId);
        }
    }

    /*
     * =========================
     * Cluster Logging
     * =========================
     */
    public static class Cluster {

        private static final String DOMAIN = "cluster";

        public static void success(Logger logger, String message, String operation, String clusterId) {
            logger.info(
                    message,
                    keyValue("domain", DOMAIN),
                    keyValue("operation", operation),
                    keyValue("cluster_id", clusterId),
                    keyValue("result", "success")
            );
        }

        public static void error(Logger logger, IotErrorCode error, String operation, String clusterId) {
            logError(logger, error, operation, "cluster_id", clusterId);
        }
    }

    /*
     * =========================
     * Core Error Logger
     * =========================
     */
    private static void logError(Logger logger, AppErrorCode error, String operation, String resourceKey, String resourceValue) {
        logger.error(
                error.message(),
                keyValue("trace_id", MDC.get("trace_id")),
                keyValue("error_code", error.code()),
                keyValue("error_key", ((Enum<?>) error).name()),
                keyValue("domain", error.domain()),
                keyValue("operation", operation),
                keyValue(resourceKey, resourceValue),
                keyValue("http_status", error.status().value()),
                keyValue("result", "failed")
        );
    }
}