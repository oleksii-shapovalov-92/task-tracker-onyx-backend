package de.upteams.tasktracker.user.controller;

import de.upteams.tasktracker.exception.handling.GlobalExceptionHandler;
import de.upteams.tasktracker.security.filter.JwtTokenFilter;
import de.upteams.tasktracker.security.service.CustomUserDetailsService;
import de.upteams.tasktracker.user.controller.impl.RegisterControllerImpl;
import de.upteams.tasktracker.user.service.impl.UserRegisterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import de.upteams.tasktracker.user.exception.UserAlreadyExistException;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(RegisterControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
public class RegisterControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRegisterService userRegisterService;

    @MockitoBean
    private JwtTokenFilter jwtTokenFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @ParameterizedTest
    @ValueSource(strings = {
            "plaintext",
            "@nodomain.com",
            "noatsign.com",
            "test@.com",
            "te st@domain.com",
            "test@@domain.com",
            "@@",
            "test@domain.c",
            "t@d.c",
            "тест@domain.com"
    })
    @DisplayName("Should return 400 Bad Request for invalid email formats")
    void shouldReturn400ForInvalidEmailFormats(String invalidEmail) throws Exception {
        String requestBody = """
                {
                  "email": "%s",
                  "password": "Test12345!"
                }
                """.formatted(invalidEmail);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));

        // Verify that the service is not called if validation fails
        verifyNoInteractions(userRegisterService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should return 400 Bad Request for blank email")
    void shouldReturn400ForBlankEmail(String invalidEmail) throws Exception {
        String requestBody = """
                {
                  "email": "%s",
                  "password": "Test12345!"
                }
                """.formatted(invalidEmail);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));

        verifyNoInteractions(userRegisterService);
    }

    @Test
    @DisplayName("Should return 409 Conflict when confirmed user with same email already exists")
    void shouldReturn409WhenConfirmedUserAlreadyExists() throws Exception {
        String requestBody = """
            {
              "email": "serdar_test@upteams.de",
              "password": "Test12345!"
            }
            """;

        when(userRegisterService.register(any()))
                .thenThrow(new UserAlreadyExistException());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User already exists"))
                .andExpect(jsonPath("$.path").value("/api/v1/users/register"));
    }
}
