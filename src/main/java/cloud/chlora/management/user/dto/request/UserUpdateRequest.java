package cloud.chlora.management.user.dto.request;

import cloud.chlora.management.user.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Email(message = "Email must be a valid email address")
        String email,

        @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
        String name,

        UserRole role
) {}