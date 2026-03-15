package cloud.chlora.management.user.controller;

import cloud.chlora.management.user.domain.enums.UserRole;
import cloud.chlora.management.user.dto.param.UserQueryParam;
import cloud.chlora.management.user.dto.request.UserCreateRequest;
import cloud.chlora.management.user.dto.request.UserUpdateRequest;
import cloud.chlora.management.user.dto.response.PagedUserResponse;
import cloud.chlora.management.user.dto.response.UserCreateResponse;
import cloud.chlora.management.user.dto.response.UserGetResponse;
import cloud.chlora.management.user.dto.response.UserUpdateResponse;
import cloud.chlora.management.user.service.UserService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<@NonNull PagedUserResponse<UserGetResponse>> findAllExistingUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "user_id") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false) UserRole role
    ) {
        var queryParam = new UserQueryParam(page, size, search, sort, order, role);
        var response = userService.findAllExistingUser(queryParam);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<@NonNull UserCreateResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserCreateResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<@NonNull UserGetResponse> findOneByUserId(@PathVariable String userId) {
        UserGetResponse response = userService.findOneByUserId(userId);
        return ResponseEntity.ok().body(response);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<@NonNull UserUpdateResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<@NonNull Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
