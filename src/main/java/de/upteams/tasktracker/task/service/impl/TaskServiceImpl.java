package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.entity.TaskStatus;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for various operations with Tasks
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TaskServiceImpl implements TaskService {

    private static final List<ProjectRoles> TASK_MUTATION_ROLES = List.of(
            ProjectRoles.MEMBER,
            ProjectRoles.OWNER,
            ProjectRoles.ADMIN
    );

    private final TaskRepository repository;
    private final TaskMappingService mappingService;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;

    @Override
    public TaskResponseDto save(final TaskCreateDto newTaskDto, final AppUser authUser) {
        final Project project = getProjectOrThrowForUser(newTaskDto.projectId(), authUser);

        checkProjectPermissionOrThrow(authUser, project);

        final Task entity = mappingService.mapCreateDtoToEntity(newTaskDto);
        entity.setProject(project);

        return mappingService.mapEntityToDto(repository.save(entity));
    }

    @Override
    public TaskResponseDto getById(String id, AppUser authUser) {
        final Task task = getOrThrow(id, authUser);

        hideTaskIfUserHasNoProjectAccess(authUser, task);

        return mappingService.mapEntityToDto(task);
    }

    @Override
    public Task getOrThrow(String id, AppUser authUser) {
        return findById(id, authUser)
                .orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public Optional<Task> findById(String id, AppUser authUser) {
        final UUID taskId;

        try {
            taskId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invalid taskId format");
        }

        return repository.findById(taskId);
    }

    @Override
    public List<TaskResponseDto> getAll(final String projectId, final AppUser authUser) {
        final Project project = getProjectOrThrowForUser(projectId, authUser);

        return repository
                .findByProject(project)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public void delete(final String id, final AppUser changer) {
        final Task existedTask = getOrThrow(id, changer);

        checkTaskPermissionOrThrow(changer, existedTask);

        repository.delete(existedTask);
    }

    @Override
    public TaskResponseDto updateStatus(
            final String id,
            final TaskStatus status,
            final AppUser changer
    ) {
        final Task existedTask = getOrThrow(id, changer);

        checkTaskPermissionOrThrow(changer, existedTask);

        existedTask.setStatus(status);

        return mappingService.mapEntityToDto(repository.save(existedTask));
    }

    @Override
    public TaskResponseDto update(
            final String id,
            final TaskUpdateDto request,
            final AppUser changer
    ) {
        final Task existedTask = getOrThrow(id, changer);

        checkTaskPermissionOrThrow(changer, existedTask);

        existedTask.setTitle(request.title());
        existedTask.setDescription(request.description());

        return mappingService.mapEntityToDto(repository.save(existedTask));
    }

    private Project getProjectOrThrowForUser(String projectId, AppUser authUser) {
        final Project project = projectService.getOrThrowById(projectId);

        hideProjectIfUserHasNoAccess(authUser, project);

        return project;
    }

    private void hideProjectIfUserHasNoAccess(AppUser user, Project project) {
        final boolean userInProject = collaboratorService.isUserInProject(user, project);

        if (!userInProject) {
            throw new ProjectNotFoundException();
        }
    }

    private void hideTaskIfUserHasNoProjectAccess(AppUser user, Task task) {
        final boolean userInProject = collaboratorService.isUserInProject(user, task.getProject());

        if (!userInProject) {
            throw new TaskNotFoundException();
        }
    }

    private void checkTaskPermissionOrThrow(AppUser user, Task task) {
        hideTaskIfUserHasNoProjectAccess(user, task);

        checkProjectPermissionOrThrow(user, task.getProject());
    }

    private void checkProjectPermissionOrThrow(AppUser user, Project project) {
        final boolean hasPermission = collaboratorService.hasUserPermission(
                user,
                project,
                TASK_MUTATION_ROLES
        );

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
    }
}