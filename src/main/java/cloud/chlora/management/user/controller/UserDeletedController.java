package cloud.chlora.management.user.controller;

import cloud.chlora.management.user.domain.enums.UserRole;
import cloud.chlora.management.user.dto.param.UserQueryParam;
import cloud.chlora.management.user.dto.response.PagedUserResponse;
import cloud.chlora.management.user.dto.response.UserDeletedResponse;
import cloud.chlora.management.user.service.UserDeletedService;
import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/deleted")
public class UserDeletedController {

    private final UserDeletedService userDeletedService;

    public UserDeletedController(UserDeletedService userDeletedService) {
        this.userDeletedService = userDeletedService;
    }

    @GetMapping
    public ResponseEntity<@NonNull PagedUserResponse<UserDeletedResponse>> findAllDeletedUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "user_id") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false) UserRole role
    ) {
        var queryParam = new UserQueryParam(page, size, search, sort, order, role);
        var response = userDeletedService.findAllDeletedUsers(queryParam);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<@NonNull UserDeletedResponse> findDeletedByUserId(@PathVariable String userId) {
        var response = userDeletedService.findDeletedByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/restore")
    public ResponseEntity<@NonNull UserDeletedResponse> restoreUser(@PathVariable String userId) {
        var response = userDeletedService.restoreUser(userId);
        return ResponseEntity.ok(response);
    }
}