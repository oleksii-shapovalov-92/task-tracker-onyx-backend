package de.upteams.tasktracker.collaborator.service;

import de.upteams.tasktracker.collaborator.persistence.CollaboratorRepository;
import de.upteams.tasktracker.collaborator.service.impl.CollaboratorServiceImpl;
import de.upteams.tasktracker.project.entity.Project;
import de.upteams.tasktracker.user.entity.AppUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollaboratorServiceImpl tests")
class CollaboratorServiceImplTest {

    @Mock
    private CollaboratorRepository collaboratorRepository;

    @InjectMocks
    private CollaboratorServiceImpl collaboratorService;

    @Test
    @DisplayName("Should recognize project owner as project member")
    void shouldRecognizeProjectOwnerAsProjectMember() {
        AppUser owner = new AppUser();
        setField(owner, "id", UUID.randomUUID());

        Project project = new Project();
        project.setOwner(owner);

        boolean result = collaboratorService.isUserInProject(owner, project);

        assertThat(result).isTrue();
        verifyNoInteractions(collaboratorRepository);
    }
}
