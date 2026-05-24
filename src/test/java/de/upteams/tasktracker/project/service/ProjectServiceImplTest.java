package de.upteams.tasktracker.project.service;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
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

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("Should throw bad request when project id format is invalid in getOrTrow")
    void shouldThrowBadRequestWhenProjectIdFormatIsInvalidInGetOrTrow() {
        assertThatThrownBy(() -> projectService.getOrTrow("invalid-id"))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid projectId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should throw bad request when project id format is invalid in delete")
    void shouldThrowBadRequestWhenProjectIdFormatIsInvalidInDelete() {
        AppUser authenticatedUser = mock(AppUser.class);

        assertThatThrownBy(() -> projectService.delete("invalid-id", authenticatedUser))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid projectId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should delete project by parsed uuid when project exists and user is owner")
    void shouldDeleteProjectByParsedUuidWhenProjectExistsAndUserIsOwner() {
        UUID projectId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();

        AppUser authenticatedUser = mock(AppUser.class);
        when(authenticatedUser.getId()).thenReturn(authenticatedUserId);

        Project project = new Project();

        project.setOwner(authenticatedUser);

        when(repository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.delete(projectId.toString(), authenticatedUser);

        verify(repository).findById(projectId);
        verify(repository).delete(project);
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when deleting non-existent project")
    void shouldThrowProjectNotFoundExceptionWhenDeletingNonExistentProject() {
        UUID projectId = UUID.randomUUID();

        AppUser authenticatedUser = mock(AppUser.class);

        when(repository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.delete(projectId.toString(), authenticatedUser))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(repository).findById(projectId);
        verify(repository, never()).delete(any(Project.class));
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when deleting project owned by another user")
    void shouldThrowProjectNotFoundExceptionWhenDeletingProjectOwnedByAnotherUser() {
        UUID projectId = UUID.randomUUID();
        UUID authenticatedUserId = UUID.randomUUID();
        UUID projectOwnerId = UUID.randomUUID();

        AppUser authenticatedUser = mock(AppUser.class);
        when(authenticatedUser.getId()).thenReturn(authenticatedUserId);

        AppUser projectOwner = mock(AppUser.class);
        when(projectOwner.getId()).thenReturn(projectOwnerId);

        Project project = new Project();

        project.setOwner(projectOwner);

        when(repository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.delete(projectId.toString(), authenticatedUser))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(repository).findById(projectId);
        verify(repository, never()).delete(any(Project.class));
    }
}