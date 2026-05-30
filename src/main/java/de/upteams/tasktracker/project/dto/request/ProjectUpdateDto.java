package de.upteams.tasktracker.project.dto.request;

import de.upteams.tasktracker.project.constants.ProjectValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating Project entity.
 */
@Schema(description = "Data Transfer Object for updating Project entity")
public record ProjectUpdateDto(
        @Schema(
                description = "Updated title of the Project",
                example = "Updated Website Development"
        )

        @Size(
                min = ProjectValidationConstats.TITLE_MIN_LENGTH,
                max = ProjectValidationConstats.TITLE_MAX_LENGTH,
                message = "Project title must be between 3 and 100 characters long"
        )
        @Pattern(
                regexp = ProjectValidationConstats.TITLE_REGEX,
                message = "Project title must start with an uppercase letter and may contain only letters, digits, spaces, dots, ampersands, apostrophes, parentheses and hyphens"
        )
        String title,

        @Schema(
                description = "Updated description of the Project",
                example = "An updated project description."
        )

        @Size(
                min = ProjectValidationConstats.DESCRIPTION_MIN_LENGTH,
                max = ProjectValidationConstats.DESCRIPTION_MAX_LENGTH,
                message = "Project description must be between 10 and 500 characters long"
        )
        @Pattern(
                regexp = ProjectValidationConstats.DESCRIPTION_REGEX,
                message = "Project description must start with an uppercase letter or digit and may contain only letters, digits, spaces and common punctuation"
        )
        String description
) {
}