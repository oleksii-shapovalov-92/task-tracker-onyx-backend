package de.upteams.tasktracker.task.service.impl;

import de.upteams.tasktracker.task.dto.request.TaskUpdateDto;
import org.springframework.transaction.annotation.Transactional;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.dto.request.TaskCreateDto;
import de.upteams.tasktracker.task.dto.response.TaskResponseDto;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.task.exception.TaskNotFoundException;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.service.interfaces.TaskService;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.task.entity.TaskStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

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

    private final TaskRepository repository;
    private final TaskMappingService mappingService;
    private final ProjectService projectService;
    private final CollaboratorService collaboratorService;

    @Override
    public TaskResponseDto save(final TaskCreateDto newTaskDto, final AppUser authUser) {
        final Project project = projectService.getOrTrow(newTaskDto.projectId(), authUser);
        final boolean userInProject = collaboratorService.isUserInProject(authUser, project);
        if (!userInProject) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }

        final Task entity = mappingService.mapCreateDtoToEntity(newTaskDto);
        entity.setProject(project);
        return mappingService.mapEntityToDto(repository.save(entity));
    }

    @Override
    public TaskResponseDto getById(String id, AppUser authUser) {
        return mappingService.mapEntityToDto(getOrThrow(id, authUser));
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

        return repository
                .findByIdAndProjectOwner(taskId, authUser);
    }

    @Override
    public List<TaskResponseDto> getAll(final String projectId, final AppUser authUser) {
        final Project project = projectService.getOrTrow(projectId, authUser);
        boolean userInProject = collaboratorService.isUserInProject(authUser, project);
        if (!userInProject) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
        return repository
                .findByProject(project)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public void delete(final String id, final AppUser changer) {
        final Task existedTask = getOrThrow(id, changer);
        final boolean hasPermission = collaboratorService.hasUserPermission(
                changer,
                existedTask.getProject(),
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );
        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }
        repository.delete(existedTask);
    }

    @Override
    public TaskResponseDto updateStatus(
            final String id,
            final TaskStatus status,
            final AppUser changer
    ) {
        final Task existedTask = getOrThrow(id, changer);

        final boolean hasPermission = collaboratorService.hasUserPermission(
                changer,
                existedTask.getProject(),
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }

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

        final boolean hasPermission = collaboratorService.hasUserPermission(
                changer,
                existedTask.getProject(),
                List.of(ProjectRoles.MEMBER, ProjectRoles.OWNER, ProjectRoles.ADMIN)
        );

        if (!hasPermission) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }

        existedTask.setTitle(request.title());
        existedTask.setDescription(request.description());

        return mappingService.mapEntityToDto(repository.save(existedTask));
    }

}
