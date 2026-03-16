package cloud.chlora.management.user.service;

import cloud.chlora.management.common.enums.UserErrorCode;
import cloud.chlora.management.common.exception.AppException;
import cloud.chlora.management.user.domain.entity.User;
import cloud.chlora.management.user.domain.enums.UserRole;
import cloud.chlora.management.user.domain.enums.UserStatus;
import cloud.chlora.management.user.dto.param.UserQueryParam;
import cloud.chlora.management.user.dto.request.UserCreateRequest;
import cloud.chlora.management.user.dto.request.UserUpdateRequest;
import cloud.chlora.management.user.dto.response.UserCreateResponse;
import cloud.chlora.management.user.dto.response.UserGetResponse;
import cloud.chlora.management.user.dto.response.UserUpdateResponse;
import cloud.chlora.management.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .userId("user-123")
                .email("test@example.com")
                .name("John Doe")
                .role(UserRole.USER)
                .createdAt(Instant.now())
                .build();
    }

    // ====== findAllExistingUser ======
    @Test
    @DisplayName("Should return paged users for valid query")
    void testFindAllExistingUser_Success() {
        UserQueryParam queryParam = new UserQueryParam(1, 10, null, "user_id", "asc", null);

        when(userRepository.findAllWithFilter(
                any(), any(), eq(UserStatus.ACTIVE), any(), any(), anyInt(), anyInt()
        )).thenReturn(List.of(user));
        when(userRepository.countUsers(any(), any(), eq(UserStatus.ACTIVE))).thenReturn(1L);

        var response = userService.findAllExistingUser(queryParam);

        assertThat(response.countData()).isEqualTo(1);
        assertThat(response.users()).hasSize(1);
        verify(userRepository).findAllWithFilter(any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    // ====== createUser ======
    @Test
    @DisplayName("Should create a new user successfully")
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest("new@example.com", "Jane Doe", UserRole.ADMIN);

        when(userRepository.findOneByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserCreateResponse response = userService.createUser(request);

        assertThat(response.email()).isEqualTo("new@example.com");
        assertThat(response.name()).isEqualTo("Jane Doe");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);

        verify(userRepository).save(any(User.class));
    }


    @Test
    @DisplayName("Should throw exception when creating user with existing email")
    void testCreateUser_EmailExists_Throws() {
        UserCreateRequest request = new UserCreateRequest("test@example.com", "John Doe", UserRole.USER);
        when(userRepository.findOneByEmail("test@example.com")).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class, () -> userService.createUser(request));
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).save(any());
    }

    // ====== findOneByUserId ======
    @Test
    @DisplayName("Should return user for valid userId")
    void testFindOneByUserId_Success() {
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));

        UserGetResponse response = userService.findOneByUserId("user-123");

        assertThat(response.userId()).isEqualTo("user-123");
        verify(userRepository).findOneByUserId("user-123");
    }

    @Test
    @DisplayName("Should throw exception if user not found by userId")
    void testFindOneByUserId_NotFound() {
        when(userRepository.findOneByUserId("missing")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> userService.findOneByUserId("missing"));
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    // ====== updateUser ======
    @Test
    @DisplayName("Should update user successfully")
    void testUpdateUser_Success() {
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));
        when(userRepository.update(any(User.class))).thenReturn(1);

        UserUpdateRequest request = new UserUpdateRequest("updated@example.com", "John Updated", UserRole.ADMIN);
        UserUpdateResponse response = userService.updateUser("user-123", request);

        assertThat(response.email()).isEqualTo("updated@example.com");
        assertThat(response.name()).isEqualTo("John Updated");
        assertThat(response.role()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception if update request is empty")
    void testUpdateUser_EmptyRequest_Throws() {
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));
        UserUpdateRequest request = new UserUpdateRequest(null, null, null);

        AppException exception = assertThrows(AppException.class,
                () -> userService.updateUser("user-123", request));
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_PATCH_EMPTY);
    }

    @Test
    @DisplayName("Should throw exception when updating user with already registered email")
    void testUpdateUser_EmailAlreadyRegistered_Throws() {
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));

        User conflictingUser = User.builder()
                .userId("user-456")
                .email("taken@example.com")
                .name("Jane Doe")
                .role(UserRole.USER)
                .createdAt(Instant.now())
                .build();

        when(userRepository.findOneByEmail("taken@example.com"))
                .thenReturn(Optional.of(conflictingUser));

        UserUpdateRequest request = new UserUpdateRequest("taken@example.com", null, null);

        AppException ex = assertThrows(AppException.class,
                () -> userService.updateUser("user-123", request));

        assertThat(ex.getErrorCode()).isEqualTo(UserErrorCode.EMAIL_ALREADY_EXISTS);

        verify(userRepository, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception if update fails in repository")
    void testUpdateUser_UpdateFails_Throws() {
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));
        when(userRepository.update(any(User.class))).thenReturn(0);

        UserUpdateRequest request = new UserUpdateRequest("new@example.com", null, null);
        AppException exception = assertThrows(AppException.class,
                () -> userService.updateUser("user-123", request));
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_UPDATE_FAILED);
    }

    // ====== deleteUser ======
    @Test
    @DisplayName("Should delete user successfully")
    void testDeleteUser_Success() {
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));
        when(userRepository.delete("user-123")).thenReturn(1);

        userService.deleteUser("user-123");

        verify(userRepository).delete("user-123");
    }

    @Test
    @DisplayName("Should throw exception if delete fails in repository")
    void testDeleteUser_DeleteFails_Throws() {
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));
        when(userRepository.delete("user-123")).thenReturn(0);

        AppException exception = assertThrows(AppException.class,
                () -> userService.deleteUser("user-123"));
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_DELETE_FAILED);
    }

    @Test
    @DisplayName("Should throw exception if user is already deleted")
    void testDeleteUser_AlreadyDeleted_Throws() {
        user.setDeletedAt(Instant.now());
        when(userRepository.findOneByUserId("user-123")).thenReturn(Optional.of(user));

        AppException exception = assertThrows(AppException.class,
                () -> userService.deleteUser("user-123"));
        assertThat(exception.getErrorCode()).isEqualTo(UserErrorCode.USER_ALREADY_DELETED);

        verify(userRepository, never()).delete(anyString());
    }
}