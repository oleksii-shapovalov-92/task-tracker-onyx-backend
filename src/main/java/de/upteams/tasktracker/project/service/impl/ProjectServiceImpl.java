package de.upteams.tasktracker.project.service.impl;

import de.upteams.tasktracker.collaborator.entity.ProjectRoles;
import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
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

/**
 * Service for various operations with Projects
 */
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
    public ProjectResponseDto getById(String id, AppUser authUser) {
        return mappingService.mapEntityToDto(getOrTrow(id, authUser));
    }

    @Override
    public Project getOrTrow(String id, AppUser authUser) {
        final UUID projectId;

        try {
            projectId = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Invalid projectId format");
        }

        return repository
                .findByIdAndOwner(projectId, authUser)
                .orElseThrow(ProjectNotFoundException::new);
    }

    @Override
    public List<ProjectResponseDto> getAll(AppUser authUser) {
        return repository
                .findAllByOwner(authUser)
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    public void delete(String id, AppUser authUser) {
        Project project = getOrTrow(id, authUser);
        repository.delete(project);
    }
}