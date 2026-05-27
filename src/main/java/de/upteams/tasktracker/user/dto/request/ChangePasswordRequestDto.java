package de.upteams.tasktracker.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static de.upteams.tasktracker.user.constants.UserValidationConstants.PASSWORD_REGEX;

public record ChangePasswordRequestDto(

        @NotBlank(message = "Current password cannot be blank")
        @Schema(
                description = "Current user password",
                example = "oldPassword123!"
        )
        String currentPassword,

        @NotBlank(message = "{user.password.notBlank}")
        @Pattern(
                regexp = PASSWORD_REGEX,
                message = "{user.password.invalid}"
        )
        @Schema(
                description = "New password. Must follow security requirements",
                example = "newPassword123!"
        )
        String newPassword
) {
}