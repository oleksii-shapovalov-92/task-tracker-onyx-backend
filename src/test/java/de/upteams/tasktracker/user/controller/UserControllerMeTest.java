package de.upteams.tasktracker.user.controller;

import de.upteams.tasktracker.security.filter.JwtTokenFilter;
import de.upteams.tasktracker.security.service.CustomUserDetailsService;
import de.upteams.tasktracker.user.controller.impl.UserControllerImpl;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController /me tests")
class UserControllerMeTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtTokenFilter jwtTokenFilter;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Should return current user data")
    void shouldReturnCurrentUserData() throws Exception {
        UserResponseDto currentUser = new UserResponseDto(
                "Risen Cumin",
                "Backend Developer",
                "Platform",
                "https://example.com/avatar.png",
                "Working on the /me endpoint",
                "risen.cumin.22@icloud.com",
                "ROLE_USER",
                ConfirmationStatus.CONFIRMED
        );

        when(userService.getCurrentUser()).thenReturn(currentUser);

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Risen Cumin"))
                .andExpect(jsonPath("$.position").value("Backend Developer"))
                .andExpect(jsonPath("$.department").value("Platform"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.bio").value("Working on the /me endpoint"))
                .andExpect(jsonPath("$.email").value("risen.cumin.22@icloud.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.confirmationStatus").value("CONFIRMED"));
    }
}
