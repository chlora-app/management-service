package cloud.chlora.management.user.dto.response;

import cloud.chlora.management.user.domain.enums.UserRole;

import java.time.Instant;

public record UserDeletedResponse(
        String userId,
        String email,
        String name,
        UserRole role,
        Instant deletedAt
) {}