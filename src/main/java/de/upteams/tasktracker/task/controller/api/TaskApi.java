package de.upteams.tasktracker.task.controller.api;

import de.upteams.tasktracker.exception.handling.response.ErrorResponseDto;
import de.upteams.tasktracker.exception.handling.response.ValidationErrorDto;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskStatusUpdateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Task controller")
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/v1/tasks")
public interface TaskApi {

    @Operation(summary = "Create/save Task", description = "Creates a new task associated with a project")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "5",
                                      "title": "Implement repository layer",
                                      "description": "Create JPA repositories for all entities",
                                      "project": { "id": "7", "title": "New Website Development" },
                                      "executors": []
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid task payload",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ValidationErrorDto.class)),
                            examples = @ExampleObject(value = """
                                    [
                                      { "field": "projectId", "messages": ["must not be blank"] }
                                    ]
                                    """))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is project member but has no permission to create tasks",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "User has no access to this project",
                                      "path": "/api/v1/tasks"
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "404", description = "Project not found or hidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Project not found",
                                      "path": "/api/v1/tasks"
                                    }
                                    """))
            )
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    TaskResponseDto save(
            @RequestBody
            @Valid
            TaskCreateDto task,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get Task", description = "Retrieves a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDto.class)))
            ,
            @ApiResponse(responseCode = "400", description = "Invalid task ID format",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Invalid taskId format",
                                      "path": "/api/v1/tasks/invalid-id"
                                    }
                                    """)))
            ,
            @ApiResponse(responseCode = "404", description = "Task not found or hidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Task not found with id: 5",
                                      "path": "/api/v1/tasks/5"
                                    }
                                    """))
            )
    })
    @GetMapping("/{id}")
    TaskResponseDto getById(
            @PathVariable
            String id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Get all Tasks for Project", description = "Retrieves all tasks under a specific project")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of tasks",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TaskResponseDto.class))))
            ,
            @ApiResponse(responseCode = "400", description = "Invalid project ID format",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Invalid projectId format",
                                      "path": "/api/v1/tasks/project/invalid-id"
                                    }
                                    """)))
            ,
            @ApiResponse(responseCode = "404", description = "Project not found or hidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Project not found",
                                      "path": "/api/v1/tasks/project/550e8400-e29b-41d4-a716-446655440000"
                                    }
                                    """))
            )
    })
    @GetMapping("/project/{projectId}")
    List<TaskResponseDto> getAll(
            @PathVariable
            String projectId,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Delete Task", description = "Deletes a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid task ID format",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "timestamp": "2025-04-26T10:00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Invalid taskId format",
                                      "path": "/api/v1/tasks/invalid-id"
                                    }
                                    """)))
            ,
            @ApiResponse(responseCode = "404", description = "Task not found or hidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
            ,
            @ApiResponse(responseCode = "403", description = "Forbidden - user is project member but has no permission to delete tasks",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class)))
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    void deleteById(
            @PathVariable
            String id,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Update Task status", description = "Updates status of an existing task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task status successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDto.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id": "550e8400-e29b-41d4-a716-446655440000",
                                      "title": "Implement Kanban board",
                                      "description": "Add drag and drop functionality",
                                      "status": "IN_PROGRESS",
                                      "project": {
                                        "id": "7",
                                        "title": "Task Tracker"
                                      },
                                      "executors": []
                                    }
                                    """))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid task ID format or invalid status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is project member but has no permission to update task status",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task not found or hidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PatchMapping("/{id}/status")
    TaskResponseDto updateStatus(
            @PathVariable
            String id,

            @RequestBody
            @Valid
            TaskStatusUpdateDto request,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );

    @Operation(summary = "Update Task", description = "Updates title and description of an existing task")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = TaskResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid task payload or invalid task ID format",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "403", description = "Forbidden - user is project member but has no permission to update tasks",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task not found or hidden",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponseDto.class))
            )
    })
    @PatchMapping("/{id}")
    TaskResponseDto update(
            @PathVariable
            String id,

            @RequestBody
            @Valid
            TaskUpdateDto request,

            @AuthenticationPrincipal
            @Parameter(hidden = true)
            AuthUserDetails principal
    );
}
