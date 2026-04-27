package de.upteams.tasktracker.project.service;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.persistence.ProjectRepository;
import de.upteams.tasktracker.project.service.impl.ProjectServiceImpl;
import de.upteams.tasktracker.project.utils.ProjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

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
        assertThatThrownBy(() -> projectService.delete("invalid-id"))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid projectId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should delete project by parsed uuid when project exists")
    void shouldDeleteProjectByParsedUuidWhenProjectExists() {
        UUID projectId = UUID.randomUUID();

        when(repository.existsById(projectId)).thenReturn(true);

        projectService.delete(projectId.toString());

        verify(repository).existsById(projectId);
        verify(repository).deleteById(projectId);
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when deleting non-existent project")
    void shouldThrowProjectNotFoundExceptionWhenDeletingNonExistentProject() {
        UUID projectId = UUID.randomUUID();

        when(repository.existsById(projectId)).thenReturn(false);

        assertThatThrownBy(() -> projectService.delete(projectId.toString()))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(repository).existsById(projectId);
        verify(repository, never()).deleteById(projectId);
    }
}
