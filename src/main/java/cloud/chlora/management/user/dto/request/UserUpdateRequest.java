package cloud.chlora.management.user.dto.request;

import cloud.chlora.management.user.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(

        @Email(message = "Email must be a valid email address")
        String email,

        @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters")
        String name,

        UserRole role,

        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$", message = "Password must contain upper, lower case letters and a number")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        String password
) {}