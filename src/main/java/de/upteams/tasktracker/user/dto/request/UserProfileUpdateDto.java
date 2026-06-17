package de.upteams.tasktracker.user.dto.request;

import de.upteams.tasktracker.user.constants.UserValidationConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for partial update of the current user's profile.
 * Null fields are treated as unchanged. Blank strings are invalid.
 */
public record UserProfileUpdateDto(

        @Pattern(
                regexp = UserValidationConstants.NON_BLANK_IF_PRESENT_REGEX,
                message = "{user.displayName.notBlank}"
        )
        @Size(max = UserValidationConstants.DISPLAY_NAME_MAX_LENGTH,
                message = "{user.displayName.size}")
        @Schema(
                description = "User's display name",
                example = "Homer Simpson"
        )
        String displayName,

        @Pattern(
                regexp = UserValidationConstants.NON_BLANK_IF_PRESENT_REGEX,
                message = "{user.position.notBlank}"
        )
        @Size(max = UserValidationConstants.POSITION_MAX_LENGTH,
                message = "{user.position.size}")
        @Schema(
                description = "User's job position",
                example = "Backend Developer"
        )
        String position,

        @Pattern(
                regexp = UserValidationConstants.NON_BLANK_IF_PRESENT_REGEX,
                message = "{user.department.notBlank}"
        )
        @Size(max = UserValidationConstants.DEPARTMENT_MAX_LENGTH,
                message = "{user.department.size}")
        @Schema(
                description = "The department or team in which the user works",
                example = "Engineering"
        )
        String department,

        @Pattern(
                regexp = UserValidationConstants.NON_BLANK_IF_PRESENT_REGEX,
                message = "{user.bio.notBlank}"
        )
        @Size(max = UserValidationConstants.BIO_MAX_LENGTH,
                message = "{user.bio.size}")
        @Schema(
                description = "Short biography of user",
                example = "Java developer with QA experience"
        )
        String bio
) {
}