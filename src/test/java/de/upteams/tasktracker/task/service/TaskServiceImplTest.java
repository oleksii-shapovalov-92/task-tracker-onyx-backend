package de.upteams.tasktracker.task.service;

import de.upteams.tasktracker.collaborator.service.interfaces.CollaboratorService;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.task.persistence.TaskRepository;
import de.upteams.tasktracker.task.service.impl.TaskServiceImpl;
import de.upteams.tasktracker.task.utils.TaskMappingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

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
        assertThatThrownBy(() -> taskService.findById("invalid-id"))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid taskId format");
                });

        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Should throw bad request when task id format is invalid in getOrThrow")
    void shouldThrowBadRequestWhenTaskIdFormatIsInvalidInGetOrThrow() {
        assertThatThrownBy(() -> taskService.getOrThrow("invalid-id"))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Invalid taskId format");
                });

        verifyNoInteractions(repository);
    }
}
