package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.project.exception.ProjectNotFoundException;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import de.upteams.tasktracker.user.entity.AppUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ProjectControllerDeleteNotFoundTest {

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existent project")
    void shouldReturn404WhenDeletingNonExistentProject() {
        String nonExistentProjectId = "00000000-0000-0000-0000-000000000000";

        ProjectService projectService = mock(ProjectService.class);
        ProjectController controller = new ProjectController(projectService);

        AppUser authUser = mock(AppUser.class);
        AuthUserDetails principal = mock(AuthUserDetails.class);

        org.mockito.Mockito.when(principal.user()).thenReturn(authUser);

        doThrow(new ProjectNotFoundException())
                .when(projectService)
                .delete(nonExistentProjectId, authUser);

        assertThatThrownBy(() -> controller.deleteById(nonExistentProjectId, principal))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectService).delete(nonExistentProjectId, authUser);
    }
}