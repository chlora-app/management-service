package cloud.chlora.management.user.domain.entity;

import cloud.chlora.management.user.domain.enums.UserRole;
import lombok.*;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {

    private Long id;
    private String userId;

    private String email;
    private String password;

    private String name;
    private UserRole role;

    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public static User create(
            String email,
            String password,
            String name,
            UserRole role,
            Instant createdAt
    ) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .role(role)
                .createdAt(createdAt)
                .build();
    }
}