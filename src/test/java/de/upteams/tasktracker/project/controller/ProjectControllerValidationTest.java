package de.upteams.tasktracker.project.controller;

import de.upteams.tasktracker.exception.handling.GlobalExceptionHandler;
import de.upteams.tasktracker.project.service.interfaces.ProjectService;
import de.upteams.tasktracker.security.filter.JwtTokenFilter;
import de.upteams.tasktracker.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ProjectControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtTokenFilter jwtTokenFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @ParameterizedTest
    @CsvSource({
            "'', 'Valid Description', title",
            "'   ', 'Valid Description', title",
            "'Valid Title', '', description",
            "'Valid Title', '   ', description"
    })
    @DisplayName("Should return 400 Bad Request for blank required project fields")
    void shouldReturn400ForBlankRequiredProjectFields(String title, String description, String invalidField)
            throws Exception {
        String requestBody = """
                {
                  "title": "%s",
                  "description": "%s"
                }
                """.formatted(title, description);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value(invalidField))
                .andExpect(jsonPath("$.errors[0].messages", hasItem(
                        invalidField.equals("title")
                                ? "Project title is required"
                                : "Project description is required"
                )));

        verifyNoInteractions(projectService);
    }

    @ParameterizedTest
    @CsvSource({
            "'Ab', 'Valid Description', title, Project title must be between 3 and 155 characters long",
            "'%s', 'Valid Description', title, Project title must be between 3 and 155 characters long",
            "'Valid Title', 'Ab', description, Project description must be between 3 and 155 characters long",
            "'Valid Title', '%s', description, Project description must be between 3 and 155 characters long"
    })
    @DisplayName("Should return 400 Bad Request for invalid project field length")
    void shouldReturn400ForInvalidProjectFieldLength(
            String rawTitle,
            String rawDescription,
            String invalidField,
            String expectedMessage
    ) throws Exception {
        String title = "%s".equals(rawTitle) ? "A".repeat(156) : rawTitle;
        String description = "%s".equals(rawDescription) ? "A".repeat(156) : rawDescription;

        String requestBody = """
                {
                  "title": "%s",
                  "description": "%s"
                }
                """.formatted(title, description);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value(invalidField))
                .andExpect(jsonPath("$.errors[0].messages", hasItem(expectedMessage)));

        verifyNoInteractions(projectService);
    }

    @ParameterizedTest
    @CsvSource(
            value = {
                    "invalid title|Valid Description|title|Project title must start with a capital letter and may contain only letters, digits and spaces",
                    "Valid Title|invalid description|description|Project description must start with a capital letter and may contain only letters, digits, spaces, and , . % : ? & ! $ ; * ( )"
            },
            delimiter = '|'
    )
    @DisplayName("Should return 400 Bad Request for invalid project field format")
    void shouldReturn400ForInvalidProjectFieldFormat(
            String title,
            String description,
            String invalidField,
            String expectedMessage
    ) throws Exception {
        String requestBody = """
                {
                  "title": "%s",
                  "description": "%s"
                }
                """.formatted(title, description);

        mockMvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value(invalidField))
                .andExpect(jsonPath("$.errors[0].messages", hasItem(expectedMessage)));

        verifyNoInteractions(projectService);
    }
}
