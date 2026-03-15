package cloud.chlora.management.user.service;

import cloud.chlora.management.common.enums.UserErrorCode;
import cloud.chlora.management.common.exception.AppException;
import cloud.chlora.management.common.helper.LogHelper;
import cloud.chlora.management.common.mapper.ResponseMapper;
import cloud.chlora.management.user.domain.entity.User;
import cloud.chlora.management.user.domain.enums.UserStatus;
import cloud.chlora.management.user.dto.param.UserQueryParam;
import cloud.chlora.management.user.dto.response.PagedUserResponse;
import cloud.chlora.management.user.dto.response.UserDeletedResponse;
import cloud.chlora.management.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDeletedService {

    private static final Logger log = LoggerFactory.getLogger(UserDeletedService.class);

    private final UserRepository userRepository;

    public UserDeletedService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PagedUserResponse<UserDeletedResponse> findAllDeletedUsers(UserQueryParam queryParam) {
        int page = Math.max(queryParam.page(), 1);
        int size = Math.min(Math.max(queryParam.size(), 1), 100);
        int offset = (page - 1) * size;

        String sortColumn = resolveSortColumn(queryParam.sort());
        String sortDirection = resolveSortDirection(queryParam.order());

        var content = userRepository.findAllWithFilter(
                queryParam.search(),
                queryParam.role(),
                UserStatus.DELETED,
                sortColumn,
                sortDirection,
                size,
                offset
        );

        long totalElements = userRepository.countUsers(
                queryParam.search(),
                queryParam.role(),
                UserStatus.DELETED
        );

        int totalPages = (int) Math.ceil((double) totalElements / size);
        var mappedContent = content.stream()
                .map(ResponseMapper.UserMapper::toDeletedResponse)
                .toList();

        return new PagedUserResponse<>(
                totalElements,
                page,
                size,
                totalPages,
                mappedContent
        );
    }

    public UserDeletedResponse findDeletedByUserId(String userId) {
        log.info("action=find_deleted_by_user_id status=started userId={}", userId);

        var user = getByUserId(userId, "findDeletedByUserId");
        checkUserExists(user, "findDeletedByUserId");

        log.info("action=find_deleted_by_user_id status=success userId={}", userId);
        return ResponseMapper.UserMapper.toDeletedResponse(user);
    }

    @Transactional
    public UserDeletedResponse restoreUser(String userId) {
        log.info("action=restore_user status=started userId={}", userId);

        var user = getByUserId(userId, "restoreUser");

        int updated = userRepository.restoreUser(userId);
        if (updated == 0) {
            log.warn("action=restore_user status=failed reason=update_failed userId={}", userId);
            throw AppException.of(UserErrorCode.USER_RESTORE_FAILED);
        }

        log.info("action=restore_user status=success userId={}", userId);
        return ResponseMapper.UserMapper.toDeletedResponse(user);
    }

    private String resolveSortColumn(String sort) {
        if (sort == null) return "u.created_at";
        return switch (sort) {
            case "user_id" -> "u.user_id";
            case "name" -> "u.name";
            case "email" -> "u.email";
            case "created_at" -> "u.created_at";
            default -> "u.created_at";
        };
    }

    private String resolveSortDirection(String order) {
        return "desc".equalsIgnoreCase(order) ? "DESC" : "ASC";
    }

    private User getByUserId(String userId, String operation) {
        return userRepository.findOneByUserId(userId)
                .orElseThrow(() -> {
                    LogHelper.User.notFound(log, UserErrorCode.USER_NOT_FOUND, operation, userId);
                    return AppException.of(UserErrorCode.USER_NOT_FOUND);
                });
    }

    private void checkUserExists(User user, String operation) {
        if (user.getDeletedAt() != null) {
            LogHelper.User.conflict(log, UserErrorCode.USER_ALREADY_DELETED, operation, user.getUserId());
            throw AppException.of(UserErrorCode.USER_ALREADY_DELETED);
        }
    }
}