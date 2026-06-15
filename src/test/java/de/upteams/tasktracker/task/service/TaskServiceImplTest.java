package de.upteams.tasktracker.task.service;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.service.impl.TaskServiceImpl;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository repository;

    @Mock
    private TaskMappingService mappingService;

    @Mock
    private ProjectService projectService;

    @Mock
    private CollaboratorService collaboratorService;

    @InjectMocks
    private TaskServiceImpl taskService;

    @Test
    @DisplayName("Should throw bad request when task id format is invalid in findById")
    void shouldThrowBadRequestWhenTaskIdFormatIsInvalidInFindById() {
        assertThatThrownBy(() -> taskService.findById("invalid-id", mock(AppUser.class)))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid taskId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should throw bad request when task id format is invalid in getOrThrow")
    void shouldThrowBadRequestWhenTaskIdFormatIsInvalidInGetOrThrow() {
        assertThatThrownBy(() -> taskService.getOrThrow("invalid-id", mock(AppUser.class)))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid taskId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should return tasks when authenticated user is project owner")
    void shouldReturnTasksWhenAuthenticatedUserIsProjectOwner() {
        AppUser owner = new AppUser();
        setField(owner, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());
        project.setOwner(owner);

        Task task = new Task("Test task", "Test task description", project);

        String projectId = project.getId().toString();

        when(projectService.getOrThrowById(projectId)).thenReturn(project);
        when(collaboratorService.isUserInProject(owner, project)).thenReturn(true);
        when(repository.findByProject(project)).thenReturn(List.of(task));

        taskService.getAll(projectId, owner);

        verify(projectService).getOrThrowById(projectId);
        verify(collaboratorService).isUserInProject(owner, project);
        verify(repository).findByProject(project);
    }

    @Test
    @DisplayName("Should update task status when user has access to project")
    void shouldUpdateTaskStatusWhenUserHasAccessToProject() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        Task task = new Task("Test task", "Test task description", project);
        UUID taskId = UUID.randomUUID();
        setField(task, "id", taskId);
        task.setStatus(TaskStatus.TODO);

        TaskResponseDto responseDto = mock(TaskResponseDto.class);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.isUserInProject(user, project)).thenReturn(true);
        when(collaboratorService.hasUserPermission(
                eq(user),
                eq(project),
                eq(List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN))
        )).thenReturn(true);
        when(repository.save(task)).thenReturn(task);
        when(mappingService.mapEntityToDto(task)).thenReturn(responseDto);

        TaskResponseDto result = taskService.updateStatus(
                taskId.toString(),
                TaskStatus.IN_PROGRESS,
                user
        );

        assertThat(task.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result).isSameAs(responseDto);

        verify(repository).findById(taskId);
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService).hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        verify(repository).save(task);
        verify(mappingService).mapEntityToDto(task);
    }

    @Test
    @DisplayName("Should throw forbidden when project member has no permission during status update")
    void shouldThrowForbiddenWhenProjectMemberHasNoPermissionDuringStatusUpdate() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        Task task = new Task("Test task", "Test task description", project);
        UUID taskId = UUID.randomUUID();
        setField(task, "id", taskId);
        task.setStatus(TaskStatus.TODO);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.isUserInProject(user, project)).thenReturn(true);
        when(collaboratorService.hasUserPermission(
                eq(user),
                eq(project),
                eq(List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN))
        )).thenReturn(false);

        assertThatThrownBy(() -> taskService.updateStatus(
                taskId.toString(),
                TaskStatus.IN_PROGRESS,
                user
        ))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getMessage()).isEqualTo("User has no access to this project");
                });

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);

        verify(repository).findById(taskId);
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService).hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        verify(repository, never()).save(any());
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should throw not found when updating status of non-existing task")
    void shouldThrowNotFoundWhenUpdatingStatusOfNonExistingTask() {
        AppUser user = new AppUser();
        UUID taskId = UUID.randomUUID();

        when(repository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateStatus(
                taskId.toString(),
                TaskStatus.IN_PROGRESS,
                user
        ))
                .isInstanceOf(TaskNotFoundException.class);

        verify(repository).findById(taskId);
        verifyNoInteractions(collaboratorService);
        verify(repository, never()).save(any());
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should throw bad request when task id format is invalid during status update")
    void shouldThrowBadRequestWhenTaskIdFormatIsInvalidDuringStatusUpdate() {
        AppUser user = new AppUser();

        assertThatThrownBy(() -> taskService.updateStatus(
                "invalid-id",
                TaskStatus.IN_PROGRESS,
                user
        ))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid taskId format");
                });

        verifyNoInteractions(repository);
        verifyNoInteractions(collaboratorService);
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should update task title and description when user has access to project")
    void shouldUpdateTaskTitleAndDescriptionWhenUserHasAccessToProject() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        Task task = new Task("Old title", "Old description", project);
        UUID taskId = UUID.randomUUID();
        setField(task, "id", taskId);

        TaskResponseDto responseDto = mock(TaskResponseDto.class);
        TaskUpdateDto request = new TaskUpdateDto("Updated task title", "Updated task description");

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.isUserInProject(user, project)).thenReturn(true);
        when(collaboratorService.hasUserPermission(
                eq(user),
                eq(project),
                eq(List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN))
        )).thenReturn(true);
        when(repository.save(task)).thenReturn(task);
        when(mappingService.mapEntityToDto(task)).thenReturn(responseDto);

        TaskResponseDto result = taskService.update(taskId.toString(), request, user);

        assertThat(task.getTitle()).isEqualTo("Updated task title");
        assertThat(task.getDescription()).isEqualTo("Updated task description");
        assertThat(result).isSameAs(responseDto);

        verify(repository).findById(taskId);
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService).hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        verify(repository).save(task);
        verify(mappingService).mapEntityToDto(task);
    }

    @Test
    @DisplayName("Should throw forbidden when project member has no permission during task update")
    void shouldThrowForbiddenWhenProjectMemberHasNoPermissionDuringTaskUpdate() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        Task task = new Task("Old title", "Old description", project);
        UUID taskId = UUID.randomUUID();
        setField(task, "id", taskId);

        TaskUpdateDto request = new TaskUpdateDto("Updated task title", "Updated task description");

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.isUserInProject(user, project)).thenReturn(true);
        when(collaboratorService.hasUserPermission(
                eq(user),
                eq(project),
                eq(List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN))
        )).thenReturn(false);

        assertThatThrownBy(() -> taskService.update(taskId.toString(), request, user))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getMessage()).isEqualTo("User has no access to this project");
                });

        assertThat(task.getTitle()).isEqualTo("Old title");
        assertThat(task.getDescription()).isEqualTo("Old description");

        verify(repository).findById(taskId);
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService).hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        verify(repository, never()).save(any());
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should throw not found when updating non-existing task")
    void shouldThrowNotFoundWhenUpdatingNonExistingTask() {
        AppUser user = new AppUser();
        UUID taskId = UUID.randomUUID();
        TaskUpdateDto request = new TaskUpdateDto("Updated task title", "Updated task description");

        when(repository.findById(taskId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(taskId.toString(), request, user))
                .isInstanceOf(TaskNotFoundException.class);

        verify(repository).findById(taskId);
        verifyNoInteractions(collaboratorService);
        verify(repository, never()).save(any());
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should throw bad request when task id format is invalid during task update")
    void shouldThrowBadRequestWhenTaskIdFormatIsInvalidDuringTaskUpdate() {
        AppUser user = new AppUser();
        TaskUpdateDto request = new TaskUpdateDto("Updated task title", "Updated task description");

        assertThatThrownBy(() -> taskService.update("invalid-id", request, user))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid taskId format");
                });

        verifyNoInteractions(repository);
        verifyNoInteractions(collaboratorService);
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should create task with project when user has access to project")
    void shouldCreateTaskWithProjectWhenUserHasAccessToProject() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());
        project.setOwner(user);

        TaskCreateDto request = new TaskCreateDto(
                "Task title",
                "Task description",
                project.getId().toString()
        );

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());

        Task savedTask = new Task();
        savedTask.setTitle(request.title());
        savedTask.setDescription(request.description());
        savedTask.setProject(project);

        TaskResponseDto responseDto = mock(TaskResponseDto.class);

        when(projectService.getOrThrowById(project.getId().toString())).thenReturn(project);
        when(collaboratorService.isUserInProject(user, project)).thenReturn(true);
        when(collaboratorService.hasUserPermission(
                eq(user),
                eq(project),
                eq(List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN))
        )).thenReturn(true);
        when(mappingService.mapCreateDtoToEntity(request)).thenReturn(task);
        when(repository.save(task)).thenReturn(savedTask);
        when(mappingService.mapEntityToDto(savedTask)).thenReturn(responseDto);

        TaskResponseDto result = taskService.save(request, user);

        assertThat(task.getProject()).isEqualTo(project);
        assertThat(result).isSameAs(responseDto);

        verify(projectService).getOrThrowById(project.getId().toString());
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService).hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        verify(mappingService).mapCreateDtoToEntity(request);
        verify(repository).save(task);
        verify(mappingService).mapEntityToDto(savedTask);
    }

    @Test
    @DisplayName("Should throw project not found when creating task without project access")
    void shouldThrowProjectNotFoundWhenCreatingTaskWithoutProjectAccess() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        TaskCreateDto request = new TaskCreateDto(
                "Task title",
                "Task description",
                project.getId().toString()
        );

        when(projectService.getOrThrowById(project.getId().toString())).thenReturn(project);
        when(collaboratorService.isUserInProject(user, project)).thenReturn(false);

        assertThatThrownBy(() -> taskService.save(request, user))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectService).getOrThrowById(project.getId().toString());
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService, never()).hasUserPermission(any(), any(), anyCollection());
        verifyNoInteractions(mappingService);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw forbidden when project member has no permission to create task")
    void shouldThrowForbiddenWhenProjectMemberHasNoPermissionToCreateTask() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        TaskCreateDto request = new TaskCreateDto(
                "Task title",
                "Task description",
                project.getId().toString()
        );

        when(projectService.getOrThrowById(project.getId().toString())).thenReturn(project);
        when(collaboratorService.isUserInProject(user, project)).thenReturn(true);
        when(collaboratorService.hasUserPermission(
                eq(user),
                eq(project),
                eq(List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN))
        )).thenReturn(false);

        assertThatThrownBy(() -> taskService.save(request, user))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
                    assertThat(ex.getMessage()).isEqualTo("User has no access to this project");
                });

        verify(projectService).getOrThrowById(project.getId().toString());
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService).hasUserPermission(
                user,
                project,
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        verifyNoInteractions(mappingService);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw project not found when loading tasks for foreign project")
    void shouldThrowProjectNotFoundWhenLoadingTasksForForeignProject() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        AppUser owner = new AppUser();
        setField(owner, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());
        project.setOwner(owner);

        String projectId = project.getId().toString();

        when(projectService.getOrThrowById(projectId)).thenReturn(project);
        when(collaboratorService.isUserInProject(user, project)).thenReturn(false);

        assertThatThrownBy(() -> taskService.getAll(projectId, user))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectService).getOrThrowById(projectId);
        verify(collaboratorService).isUserInProject(user, project);
        verifyNoInteractions(repository);
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should throw task not found when loading task from foreign project")
    void shouldThrowTaskNotFoundWhenLoadingTaskFromForeignProject() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        Task task = new Task("Test task", "Test task description", project);
        UUID taskId = UUID.randomUUID();
        setField(task, "id", taskId);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.isUserInProject(user, project)).thenReturn(false);

        assertThatThrownBy(() -> taskService.getById(taskId.toString(), user))
                .isInstanceOf(TaskNotFoundException.class);

        verify(repository).findById(taskId);
        verify(collaboratorService).isUserInProject(user, project);
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should throw task not found when updating status of task from foreign project")
    void shouldThrowTaskNotFoundWhenUpdatingStatusOfTaskFromForeignProject() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        Task task = new Task("Test task", "Test task description", project);
        UUID taskId = UUID.randomUUID();
        setField(task, "id", taskId);
        task.setStatus(TaskStatus.TODO);

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.isUserInProject(user, project)).thenReturn(false);

        assertThatThrownBy(() -> taskService.updateStatus(
                taskId.toString(),
                TaskStatus.IN_PROGRESS,
                user
        ))
                .isInstanceOf(TaskNotFoundException.class);

        assertThat(task.getStatus()).isEqualTo(TaskStatus.TODO);

        verify(repository).findById(taskId);
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService, never()).hasUserPermission(any(), any(), anyCollection());
        verify(repository, never()).save(any());
        verifyNoInteractions(mappingService);
    }

    @Test
    @DisplayName("Should throw task not found when updating task from foreign project")
    void shouldThrowTaskNotFoundWhenUpdatingTaskFromForeignProject() {
        AppUser user = new AppUser();
        setField(user, "id", UUID.randomUUID());

        Project project = new Project();
        setField(project, "id", UUID.randomUUID());

        Task task = new Task("Old title", "Old description", project);
        UUID taskId = UUID.randomUUID();
        setField(task, "id", taskId);

        TaskUpdateDto request = new TaskUpdateDto("Updated task title", "Updated task description");

        when(repository.findById(taskId)).thenReturn(Optional.of(task));
        when(collaboratorService.isUserInProject(user, project)).thenReturn(false);

        assertThatThrownBy(() -> taskService.update(taskId.toString(), request, user))
                .isInstanceOf(TaskNotFoundException.class);

        assertThat(task.getTitle()).isEqualTo("Old title");
        assertThat(task.getDescription()).isEqualTo("Old description");

        verify(repository).findById(taskId);
        verify(collaboratorService).isUserInProject(user, project);
        verify(collaboratorService, never()).hasUserPermission(any(), any(), anyCollection());
        verify(repository, never()).save(any());
        verifyNoInteractions(mappingService);
    }
}