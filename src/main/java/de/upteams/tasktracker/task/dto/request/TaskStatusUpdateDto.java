package de.upteams.tasktracker.task.dto.request;

import de.upteams.tasktracker.task.entity.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Data transfer object to update Task status")
public record TaskStatusUpdateDto(

        @Schema(
                description = "New status of the Task",
                example = "IN_PROGRESS"
        )
        @NotNull
        TaskStatus status
) {
}