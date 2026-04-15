package de.upteams.tasktracker.user.dto.request;

import de.upteams.tasktracker.user.validation.ValidEmail;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import static de.upteams.tasktracker.user.constants.UserValidationConstats.PASSWORD_REGEX;

public record UserCreateDto(

        @NotBlank(message = "{user.email.notBlank}")
        @ValidEmail
        @Schema(
                description = "new User email",
                example = "tes_dev@upteams.de"
        )
        String email,

        @NotBlank(message = "{user.password.notBlank}")
        @Pattern(
                regexp = PASSWORD_REGEX,
                message = "{user.password.invalid}"
        )
        @Schema(
                description = "new User password",
                example = "dev_TR_pass_007"
        )
        String password) {
}
