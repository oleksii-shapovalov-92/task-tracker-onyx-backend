package de.upteams.tasktracker.task.service.impl;

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
        final Project project = projectService.getOrTrow(newTaskDto.projectId());
        final boolean userInProject = collaboratorService.isUserInProject(authUser, project);
        if (!userInProject) {
            throw new RestApiException(HttpStatus.FORBIDDEN, "User has no access to this project");
        }

        final Task entity = mappingService.mapCreateDtoToEntity(newTaskDto);
        entity.setProject(project);
        return mappingService.mapEntityToDto(repository.save(entity));
    }

    @Override
    public TaskResponseDto getById(String id) {
        return mappingService.mapEntityToDto(getOrThrow(id));
    }

    @Override
    public Task getOrThrow(String id) {
        return findById(id)
                .orElseThrow(TaskNotFoundException::new);
    }

    @Override
    public Optional<Task> findById(String id) {
        final UUID taskId;

        try {
            taskId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invalid taskId format");
        }

        return repository
                .findById(taskId);
    }

    @Override
    public List<TaskResponseDto> getAll(final String projectId, final AppUser authUser) {
        final Project project = projectService.getOrTrow(projectId);
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
        final Task existedTask = getOrThrow(id);
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

}
