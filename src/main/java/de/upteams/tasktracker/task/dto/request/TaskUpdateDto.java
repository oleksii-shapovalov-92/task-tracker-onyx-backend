package de.upteams.tasktracker.task.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object to update Task title and description")
public record TaskUpdateDto(

        @Schema(
                description = "Updated title of the Task",
                example = "Updated task title"
        )
        @NotBlank
        String title,

        @Schema(
                description = "Updated description of the Task",
                example = "Updated task description"
        )
        String description
) {
}