package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository repository;
    private final ProjectMapper mappingService;
    private final CollaboratorService collaboratorService;

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AppUser projectOwner) {
        Project project = mappingService.mapDtoToEntity(newProjectDto);
        project.setOwner(projectOwner);

        Project savedProject = repository.save(project);

        collaboratorService.save(projectOwner, savedProject, List.of(ProjectRoles.OWNER));

        return mappingService.mapEntityToDto(savedProject);
    }

    @Override
    public ProjectResponseDto getById(String id) {
        return mappingService.mapEntityToDto(getOrTrow(id));
    }

    @Override
    public Project getOrTrow(String id) {
        final UUID projectId;

        try {
            projectId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invalid projectId format");
        }

        return repository
                .findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public List<ProjectResponseDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public ProjectResponseDto update(
            String id,
            ProjectUpdateDto request,
            AppUser authenticatedUser
    ) {
        Project project = getOrTrow(id);

        if (!project.getOwner().getId().equals(authenticatedUser.getId())) {
            throw new ProjectNotFoundException();
        }

        if (request.title() != null) {
            project.setTitle(request.title());
        }

        if (request.description() != null) {
            project.setDescription(request.description());
        }

        return mappingService.mapEntityToDto(repository.save(project));
    }

    @Override
    public void delete(String id, AppUser authenticatedUser) {
        final UUID projectId;

        try {
            projectId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invalid projectId format");
        }

        Project project = repository
                .findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        if (!project.getOwner().getId().equals(authenticatedUser.getId())) {
            throw new ProjectNotFoundException();
        }

        repository.delete(project);
    }
}