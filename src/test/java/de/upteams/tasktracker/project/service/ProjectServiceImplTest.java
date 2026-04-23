package de.upteams.tasktracker.project.service;

import de.upteams.tasktracker.project.entity.Project;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository repository;

    @Mock
    private ProjectMapper mappingService;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("Should delete project when it exists")
    void shouldDeleteProjectWhenItExists() {
        String id = "00000000-0000-0000-0000-000000000001";
        UUID uuid = UUID.fromString(id);
        Project project = new Project();

        when(repository.findById(uuid)).thenReturn(Optional.of(project));

        projectService.delete(id);

        verify(repository).findById(uuid);
        verify(repository).delete(project);
        verify(repository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when deleting non-existent project")
    void shouldThrowProjectNotFoundExceptionWhenDeletingNonExistentProject() {
        String id = "00000000-0000-0000-0000-000000000000";
        UUID uuid = UUID.fromString(id);

        when(repository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(ProjectNotFoundException.class, () -> projectService.delete(id));

        verify(repository).findById(uuid);
        verify(repository, never()).delete(any(Project.class));
        verify(repository, never()).deleteById(any());
    }
}