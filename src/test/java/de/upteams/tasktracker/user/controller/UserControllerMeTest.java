package de.upteams.tasktracker.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.upteams.tasktracker.security.filter.JwtTokenFilter;
import de.upteams.tasktracker.security.service.CustomUserDetailsService;
import de.upteams.tasktracker.user.controller.impl.UserControllerImpl;
import de.upteams.tasktracker.user.dto.request.UserProfileUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.exception.handling.GlobalExceptionHandler;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.user.dto.request.ChangePasswordRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserControllerImpl.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("UserController /me tests")
class UserControllerMeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Test
    @DisplayName("Should update current user profile")
    void shouldUpdateCurrentUserProfile() throws Exception {
        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "Risen Cumin",
                "Backend Developer",
                "Platform",
                "Working on the PATCH /me endpoint"
        );

        UserResponseDto updatedUser = new UserResponseDto(
                "Risen Cumin",
                "Backend Developer",
                "Platform",
                "https://example.com/avatar.png",
                "Working on the PATCH /me endpoint",
                "risen.cumin.22@icloud.com",
                "ROLE_USER",
                ConfirmationStatus.CONFIRMED
        );

        when(userService.updateCurrentUserProfile(any(UserProfileUpdateDto.class))).thenReturn(updatedUser);

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Risen Cumin"))
                .andExpect(jsonPath("$.position").value("Backend Developer"))
                .andExpect(jsonPath("$.department").value("Platform"))
                .andExpect(jsonPath("$.avatarUrl").value("https://example.com/avatar.png"))
                .andExpect(jsonPath("$.bio").value("Working on the PATCH /me endpoint"))
                .andExpect(jsonPath("$.email").value("risen.cumin.22@icloud.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.confirmationStatus").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Should return bad request when display name exceeds max length")
    void shouldReturnBadRequestWhenDisplayNameExceedsMaxLength() throws Exception {
        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "a".repeat(101),
                "Backend Developer",
                "Platform",
                "Working on validation"
        );

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value("displayName"));
    }

    @Test
    @DisplayName("Should return bad request when position exceeds max length")
    void shouldReturnBadRequestWhenPositionExceedsMaxLength() throws Exception {
        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "Risen Cumin",
                "a".repeat(101),
                "Platform",
                "Working on validation"
        );

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value("position"));
    }

    @Test
    @DisplayName("Should return bad request when department exceeds max length")
    void shouldReturnBadRequestWhenDepartmentExceedsMaxLength() throws Exception {
        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "Risen Cumin",
                "Backend Developer",
                "a".repeat(101),
                "Working on validation"
        );

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value("department"));
    }

    @Test
    @DisplayName("Should return bad request when bio exceeds max length")
    void shouldReturnBadRequestWhenBioExceedsMaxLength() throws Exception {
        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "Risen Cumin",
                "Backend Developer",
                "Platform",
                "a".repeat(1001)
        );

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value("bio"));
    }

    @Test
    @DisplayName("Should update current user avatar")
    void shouldUpdateCurrentUserAvatar() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar-content".getBytes()
        );

        UserResponseDto updatedUser = new UserResponseDto(
                "Risen Cumin",
                "Backend Developer",
                "Platform",
                "https://test-bucket.fra1.digitaloceanspaces.com/avatars/user-id/avatar.png",
                "Working on avatar upload",
                "risen.cumin.22@icloud.com",
                "ROLE_USER",
                ConfirmationStatus.CONFIRMED
        );

        when(userService.updateCurrentUserAvatar(any(MultipartFile.class))).thenReturn(updatedUser);

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(avatar)
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Risen Cumin"))
                .andExpect(jsonPath("$.position").value("Backend Developer"))
                .andExpect(jsonPath("$.department").value("Platform"))
                .andExpect(jsonPath("$.avatarUrl")
                        .value("https://test-bucket.fra1.digitaloceanspaces.com/avatars/user-id/avatar.png"))
                .andExpect(jsonPath("$.bio").value("Working on avatar upload"))
                .andExpect(jsonPath("$.email").value("risen.cumin.22@icloud.com"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"))
                .andExpect(jsonPath("$.confirmationStatus").value("CONFIRMED"));
    }

    @Test
    @DisplayName("Should change current user password")
    void shouldChangeCurrentUserPassword() throws Exception {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "OldPassword123!",
                "NewPassword123!"
        );

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(userService).changeCurrentUserPassword(any(ChangePasswordRequestDto.class));
    }

    @Test
    @DisplayName("Should return bad request when current password is incorrect")
    void shouldReturnBadRequestWhenCurrentPasswordIsIncorrect() throws Exception {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "WrongPassword123!",
                "NewPassword123!"
        );

        doThrow(new RestApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect"))
                .when(userService)
                .changeCurrentUserPassword(any(ChangePasswordRequestDto.class));

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Current password is incorrect"))
                .andExpect(jsonPath("$.path").value("/api/v1/users/me/password"));
    }

    @Test
    @DisplayName("Should return bad request when new password is invalid")
    void shouldReturnBadRequestWhenNewPasswordIsInvalid() throws Exception {
        String requestBody = """
                {
                  "currentPassword": "OldPassword123!",
                  "newPassword": "weak"
                }
                """;

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.errors[0].field").value("newPassword"));

        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should return bad request when profile fields are blank")
    void shouldReturnBadRequestWhenProfileFieldsAreBlank() throws Exception {
        String requestBody = """
                {
                  "displayName": "",
                  "position": "   ",
                  "department": "",
                  "bio": "   "
                }
                """;

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"));
    }

    @Test
    @DisplayName("Should update current user avatar via POST")
    void shouldUpdateCurrentUserAvatarViaPost() throws Exception {
        MockMultipartFile avatar = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "avatar-content".getBytes()
        );

        UserResponseDto updatedUser = new UserResponseDto(
                "Risen Cumin",
                "Backend Developer",
                "Platform",
                "http://localhost:8080/uploads/avatars/user-id/avatar.png",
                "Working on avatar upload",
                "risen.cumin.22@icloud.com",
                "ROLE_USER",
                ConfirmationStatus.CONFIRMED
        );

        when(userService.updateCurrentUserAvatar(any(MultipartFile.class))).thenReturn(updatedUser);

        mockMvc.perform(multipart("/api/v1/users/me/avatar")
                        .file(avatar))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl")
                        .value("http://localhost:8080/uploads/avatars/user-id/avatar.png"));
    }
}
