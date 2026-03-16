package cloud.chlora.management.user.service;

import cloud.chlora.management.common.enums.UserErrorCode;
import cloud.chlora.management.common.exception.AppException;
import cloud.chlora.management.common.helper.LogHelper;
import cloud.chlora.management.common.mapper.ResponseMapper;
import cloud.chlora.management.user.domain.entity.User;
import cloud.chlora.management.user.domain.enums.UserStatus;
import cloud.chlora.management.user.dto.param.UserQueryParam;
import cloud.chlora.management.user.dto.request.UserCreateRequest;
import cloud.chlora.management.user.dto.request.UserUpdateRequest;
import cloud.chlora.management.user.dto.response.PagedUserResponse;
import cloud.chlora.management.user.dto.response.UserCreateResponse;
import cloud.chlora.management.user.dto.response.UserGetResponse;
import cloud.chlora.management.user.dto.response.UserUpdateResponse;
import cloud.chlora.management.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public PagedUserResponse<UserGetResponse> findAllExistingUser(UserQueryParam queryParam) {
        int page = Math.max(queryParam.page(), 1);
        int size = Math.min(Math.max(queryParam.size(), 1), 100);
        int offset = (page - 1) * size;

        String sortColumn = resolveSortColumn(queryParam.sort());
        String sortDirection = resolveSortDirection(queryParam.order());

        var content = userRepository.findAllWithFilter(
                queryParam.search(),
                queryParam.role(),
                UserStatus.ACTIVE,
                sortColumn,
                sortDirection,
                size,
                offset
        );

        long totalElements = userRepository.countUsers(
                queryParam.search(),
                queryParam.role(),
                UserStatus.ACTIVE
        );

        int totalPages = (int) Math.ceil((double) totalElements / size);

        var mappedContent = content.stream()
                .map(ResponseMapper.UserMapper::toGetResponse)
                .toList();

        return new PagedUserResponse<>(totalElements, page, size, totalPages, mappedContent);
    }

    private String resolveSortColumn(String sort) {
        return switch (sort) {
            case "user_id" -> "u.user_id";
            case "name" -> "u.name";
            case "email" -> "u.email";
            case "role" -> "u.role";
            case "created_at" -> "u.created_at";
            default -> "u.created_at";
        };
    }

    private String resolveSortDirection(String order) {
        return (order != null && order.equalsIgnoreCase("desc")) ? "DESC" : "ASC";
    }

    @Transactional
    public UserCreateResponse createUser(@Valid UserCreateRequest request) {
        if (userRepository.findOneByEmail(request.email()).isPresent()) {
            LogHelper.User.notFound(log, UserErrorCode.EMAIL_ALREADY_EXISTS, "createUser", request.email());
            throw AppException.of(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        String password = generateDefaultPassword(request.name());

        User user = User.create(
                request.email(),
                password,
                request.name(),
                request.role(),
                Instant.now()
        );

        User savedUser = userRepository.save(user);
        LogHelper.User.success(log, "User created successfully", "createUser", savedUser.getUserId());

        return ResponseMapper.UserMapper.toCreateResponse(savedUser);
    }

    public UserGetResponse findOneByUserId(String userId) {
        User user = getByUserId(userId, "findOneByUserId");
        checkUserExists(user, "findOneByUserId");

        LogHelper.User.success(log, "User found successfully", "findOneByUserId", userId);
        return ResponseMapper.UserMapper.toGetResponse(user);
    }

    @Transactional
    public UserUpdateResponse updateUser(String userId, UserUpdateRequest request) {
        User existingUser = getByUserId(userId, "updateUser");
        checkUserExists(existingUser, "updateUser");

        if (request.email() == null && request.name() == null && request.role() == null) {
            LogHelper.User.error(log, UserErrorCode.USER_PATCH_EMPTY, "updateUser", userId);
            throw AppException.of(UserErrorCode.USER_PATCH_EMPTY);
        }

        if (request.email() != null && !request.email().equals(existingUser.getEmail())) {
            if (userRepository.findOneByEmail(request.email()).isPresent()) {
                LogHelper.User.conflict(log, UserErrorCode.EMAIL_ALREADY_EXISTS, "updateUser", request.email());
                throw AppException.of(UserErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }

        User updatedUser = existingUser.toBuilder()
                .email(request.email() != null ? request.email() : existingUser.getEmail())
                .name(request.name() != null ? request.name() : existingUser.getName())
                .role(request.role() != null ? request.role() : existingUser.getRole())
                .updatedAt(Instant.now())
                .build();

        int rows = userRepository.update(updatedUser);
        if (rows == 0) {
            LogHelper.User.error(log, UserErrorCode.USER_UPDATE_FAILED, "updateUser", updatedUser.getUserId());
            throw AppException.of(UserErrorCode.USER_UPDATE_FAILED);
        }

        return ResponseMapper.UserMapper.toUpdateResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = getByUserId(userId, "deleteUser");
        checkUserExists(user, "deleteUser");

        var rows = userRepository.delete(userId);
        if (rows == 0) {
            LogHelper.User.error(log, UserErrorCode.USER_DELETE_FAILED, "deleteUser", userId);
            throw AppException.of(UserErrorCode.USER_DELETE_FAILED);
        }

        LogHelper.User.success(log, "User deleted successfully", "deleteUser", userId);
    }

    // ===== HELPER =====
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

    private String generateDefaultPassword(String name) {
        return "chlora_" + name.split(" ")[0];
    }
}
