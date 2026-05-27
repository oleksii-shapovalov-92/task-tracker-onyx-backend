package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.project.controller.api.ProjectApi;
import de.upteams.tasktracker.security.service.AuthUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Project controller contract tests")
class ProjectControllerContractTest {

    @Test
    @DisplayName("Delete endpoint should declare 204 no content")
    void deleteEndpointShouldDeclare204NoContent() throws NoSuchMethodException {

        Method deleteMethod = ProjectApi.class.getMethod(
                "deleteById",
                String.class,
                AuthUserDetails.class
        );

        ResponseStatus responseStatus = deleteMethod.getAnnotation(ResponseStatus.class);

        assertThat(responseStatus).isNotNull();
        assertThat(responseStatus.value()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Controller should implement delete endpoint with void return type")
    void controllerShouldImplementDeleteEndpointWithVoidReturnType() throws NoSuchMethodException {

        Method deleteMethod = ProjectController.class.getMethod(
                "deleteById",
                String.class,
                AuthUserDetails.class
        );

        assertThat(deleteMethod.getReturnType()).isEqualTo(void.class);
    }
}