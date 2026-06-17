package de.upteams.tasktracker.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import de.upteams.tasktracker.task.constants.TaskValidationConstats;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object to update Task title and description")
public record TaskUpdateDto(

        @Schema(
                description = "Updated title of the Task",
                example = "Updated task title"
        )
        @NotBlank
        @Pattern(
                regexp = TaskValidationConstats.TITLE_REGEX,
                message = TaskValidationConstats.TITLE_MESSAGE
        )
        String title,

        @Schema(
                description = "Updated description of the Task",
                example = "Updated task description"
        )
        @Pattern(
                regexp = TaskValidationConstats.DESCRIPTION_REGEX,
                message = TaskValidationConstats.DESCRIPTION_MESSAGE
        )
        String description
) {
}