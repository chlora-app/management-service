package cloud.chlora.management.user.dto.response;

import java.util.List;

public record PagedUserResponse<T>(
        Long countData,
        int page,
        int size,
        int totalPages,
        List<T> users
) {}