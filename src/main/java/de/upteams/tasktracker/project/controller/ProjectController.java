package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.project.dto.request.ProjectCreateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProjectController implements ProjectApi {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @Override
    public ProjectResponseDto save(ProjectCreateDto newProjectDto, AuthUserDetails principal) {
        return service.save(newProjectDto, principal.user());
    }

    @Override
    public ProjectResponseDto getById(String id, AuthUserDetails principal) {
        return service.getById(id, principal.user());
    }

    @Override
    public List<ProjectResponseDto> getAll(AuthUserDetails principal) {
        return service.getAll(principal.user());
    }

    @Override
    public void deleteById(String id, AuthUserDetails principal) {
        service.delete(id, principal.user());
    }
}