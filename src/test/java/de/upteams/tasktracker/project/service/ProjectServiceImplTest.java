package de.upteams.tasktracker.project.service;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.dto.request.ProjectUpdateDto;
import de.upteams.tasktracker.project.dto.response.ProjectResponseDto;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.impl.ProjectServiceImpl;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import de.upteams.tasktracker.user.entity.AppUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectServiceImpl tests")
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository repository;

    @Mock
    private ProjectMapper mappingService;

    @Mock
    private AppUser authUser;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("Should throw bad request when project id format is invalid in getOrTrow")
    void shouldThrowBadRequestWhenProjectIdFormatIsInvalidInGetOrTrow() {
        assertThatThrownBy(() -> projectService.getOrTrow("invalid-id", authUser))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid projectId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should throw bad request when project id format is invalid in delete")
    void shouldThrowBadRequestWhenProjectIdFormatIsInvalidInDelete() {
        assertThatThrownBy(() -> projectService.delete("invalid-id", authUser))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid projectId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should delete project by parsed uuid when project exists and belongs to owner")
    void shouldDeleteProjectByParsedUuidWhenProjectExistsAndBelongsToOwner() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();

        when(repository.findByIdAndOwner(projectId, authUser)).thenReturn(Optional.of(project));

        projectService.delete(projectId.toString(), authUser);

        verify(repository).findByIdAndOwner(projectId, authUser);
        verify(repository).delete(project);
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when deleting non-existent or foreign project")
    void shouldThrowProjectNotFoundExceptionWhenDeletingNonExistentOrForeignProject() {
        UUID projectId = UUID.randomUUID();

        when(repository.findByIdAndOwner(projectId, authUser)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(projectId.toString(), authUser))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(repository).findByIdAndOwner(projectId, authUser);
        verify(repository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("Should update project when authenticated user is owner")
    void shouldUpdateProjectWhenAuthenticatedUserIsOwner() {
        UUID projectId = UUID.randomUUID();

        Project project = new Project();
        project.setTitle("Old title");
        project.setDescription("Old description");

        ProjectUpdateDto request =
                new ProjectUpdateDto("New title", "New description");

        ProjectResponseDto response =
                mock(ProjectResponseDto.class);

        when(repository.findByIdAndOwner(projectId, authUser))
                .thenReturn(Optional.of(project));

        when(repository.save(project))
                .thenReturn(project);

        when(mappingService.mapEntityToDto(project))
                .thenReturn(response);

        ProjectResponseDto result =
                projectService.update(
                        projectId.toString(),
                        request,
                        authUser
                );

        assertThat(result).isEqualTo(response);

        assertThat(project.getTitle()).isEqualTo("New title");
        assertThat(project.getDescription()).isEqualTo("New description");

        verify(repository).findByIdAndOwner(projectId, authUser);
        verify(repository).save(project);
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when updating project owned by another user")
    void shouldThrowProjectNotFoundExceptionWhenUpdatingProjectOwnedByAnotherUser() {
        UUID projectId = UUID.randomUUID();

        ProjectUpdateDto request =
                new ProjectUpdateDto("New title", "New description");

        when(repository.findByIdAndOwner(projectId, authUser))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                projectService.update(
                        projectId.toString(),
                        request,
                        authUser
                ))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(repository).findByIdAndOwner(projectId, authUser);
        verify(repository, never()).save(any(Project.class));
    }
}