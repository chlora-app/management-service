package cloud.chlora.management.user.domain.entity;

import cloud.chlora.management.user.domain.enums.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
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

    public User() {}

    public User(
            String email,
            String password,
            String name,
            UserRole role,
            Instant createdAt
    ) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.createdAt = createdAt;
    }
}