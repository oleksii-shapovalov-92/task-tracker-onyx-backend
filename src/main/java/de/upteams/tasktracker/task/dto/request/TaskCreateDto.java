package de.upteams.tasktracker.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

/**
 * Task create DTO
 */

@Schema(description = "Data transfer object to create Task entity")
public record TaskCreateDto(

        @Schema(
                description = "Title of the Task",
                example = "Implement repository layer")
        @NotBlank
        @Pattern(
                regexp = TaskValidationConstats.TITLE_REGEX,
                message = TaskValidationConstats.TITLE_MESSAGE
        )
        String title,

        @Schema(
                description = "Detailed description of the Task",
                example = "Create JPA repositories for all entities"
        )
        @Pattern(
                regexp = TaskValidationConstats.DESCRIPTION_REGEX,
                message = TaskValidationConstats.DESCRIPTION_MESSAGE
        )
        String description,

        @Schema(
                description = "Identifier of the Project this Task belongs to",
                example = "5"
        )
        @NotBlank
        String projectId
) {
}
