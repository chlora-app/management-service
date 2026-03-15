package cloud.chlora.management.user.dto.param;

import cloud.chlora.management.user.domain.enums.UserRole;

public record UserQueryParam(
        int page,
        int size,
        String search,
        String sort,
        String order,
        UserRole role
) {}