package de.upteams.tasktracker.user.dto.response;

import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO returned when fetching user data.
 */
@Schema(description = "User data returned by the API")
public record UserResponseDto(

        @Schema(
                description = "User's display name",
                example = "Homer Simpson"
        )
        String displayName,

        @Schema(
                description = "User's job position",
                example = "developer"
        )
        String position,

        @Schema(
                description = "The department or team in which the user works",
                example = "Mobile Development"
        )
        String department,

        @Schema(
                description = "User's avatar URL",
                example = "https://example.com/avatars/homer.png"
        )
        String avatarUrl,

        @Schema(
                description = "Short biography of user",
                example = "Fullstack developer, QA"
        )
        String bio,

        @Schema(
                description = "User's email address",
                example = "homer@simpsons.com"
        )
        String email,

        @Schema(
                description = "Role assigned to the user",
                example = "ROLE_USER",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        String role,

        @Schema(
                description = "Confirmation status of the user account",
                example = "UNCONFIRMED",
                accessMode = Schema.AccessMode.READ_ONLY
        )
        ConfirmationStatus confirmationStatus


) {
}
