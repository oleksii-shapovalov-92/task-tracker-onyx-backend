package de.upteams.tasktracker.user.service;

import de.upteams.tasktracker.configuration.AwsS3Configuration;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.files.uploading.FileService;
import de.upteams.tasktracker.user.constants.UserValidationConstants;
import de.upteams.tasktracker.user.dto.request.UserProfileUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.entity.Role;
import de.upteams.tasktracker.user.exception.UserNotFoundException;
import de.upteams.tasktracker.user.persistence.UserRepository;
import de.upteams.tasktracker.user.service.impl.UserServiceImpl;
import de.upteams.tasktracker.user.util.AppUserMapper;
import de.upteams.tasktracker.user.dto.request.ChangePasswordRequestDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl tests")
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private AppUserMapper mappingService;

    @Mock
    private FileService fileService;

    @Mock
    private AwsS3Configuration awsS3Configuration;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should return current user from security context")
    void shouldReturnCurrentUserFromSecurityContext() {
        AppUser user = new AppUser("encoded-password", "risen.cumin.22@icloud.com");
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        user.setDisplayName("");
        user.setPosition("");
        user.setDepartment("");
        user.setBio("");
        user.setAvatarUrl(null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));

        UserResponseDto result = userService.getCurrentUser();

        assertThat(result.email()).isEqualTo("risen.cumin.22@icloud.com");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationStatus()).isEqualTo(ConfirmationStatus.CONFIRMED);
        assertThat(result.displayName()).isEqualTo("");
        assertThat(result.position()).isEqualTo("");
        assertThat(result.department()).isEqualTo("");
        assertThat(result.bio()).isEqualTo("");
        assertThat(result.avatarUrl()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when current user is not found")
    void shouldThrowExceptionWhenCurrentUserIsNotFound() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getCurrentUser())
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should return current user with filled profile data")
    void shouldReturnCurrentUserWithFilledProfileData() {
        AppUser user = new AppUser("encoded-password", "risen.cumin.22@icloud.com");
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        user.setDisplayName("Risen Cumin");
        user.setPosition("Backend Developer");
        user.setDepartment("Platform");
        user.setBio("Works on current user endpoint");
        user.setAvatarUrl("https://example.com/avatar.png");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));

        UserResponseDto result = userService.getCurrentUser();

        assertThat(result.displayName()).isEqualTo("Risen Cumin");
        assertThat(result.position()).isEqualTo("Backend Developer");
        assertThat(result.department()).isEqualTo("Platform");
        assertThat(result.bio()).isEqualTo("Works on current user endpoint");
        assertThat(result.avatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(result.email()).isEqualTo("risen.cumin.22@icloud.com");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationStatus()).isEqualTo(ConfirmationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should update current user profile fields")
    void shouldUpdateCurrentUserProfileFields() {
        AppUser user = new AppUser("encoded-password", "risen.cumin.22@icloud.com");
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        user.setDisplayName("Old Name");
        user.setPosition("Old Position");
        user.setDepartment("Old Department");
        user.setBio("Old bio");
        user.setAvatarUrl(null);

        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "  New Name  ",
                "  Backend Developer  ",
                "  Platform  ",
                "  Updated bio  "
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));

        UserResponseDto result = userService.updateCurrentUserProfile(request);

        assertThat(result.displayName()).isEqualTo("New Name");
        assertThat(result.position()).isEqualTo("Backend Developer");
        assertThat(result.department()).isEqualTo("Platform");
        assertThat(result.bio()).isEqualTo("Updated bio");
        assertThat(result.email()).isEqualTo("risen.cumin.22@icloud.com");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationStatus()).isEqualTo(ConfirmationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should keep existing profile fields when request values are null")
    void shouldKeepExistingProfileFieldsWhenRequestValuesAreNull() {
        AppUser user = new AppUser("encoded-password", "risen.cumin.22@icloud.com");
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        user.setDisplayName("Current Name");
        user.setPosition("Current Position");
        user.setDepartment("Current Department");
        user.setBio("Current bio");
        user.setAvatarUrl(null);

        UserProfileUpdateDto request = new UserProfileUpdateDto(
                null,
                null,
                null,
                null
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));

        UserResponseDto result = userService.updateCurrentUserProfile(request);

        assertThat(result.displayName()).isEqualTo("Current Name");
        assertThat(result.position()).isEqualTo("Current Position");
        assertThat(result.department()).isEqualTo("Current Department");
        assertThat(result.bio()).isEqualTo("Current bio");
        assertThat(result.email()).isEqualTo("risen.cumin.22@icloud.com");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationStatus()).isEqualTo(ConfirmationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should clear profile fields when request values are blank")
    void shouldClearProfileFieldsWhenRequestValuesAreBlank() {
        AppUser user = new AppUser("encoded-password", "risen.cumin.22@icloud.com");
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        user.setDisplayName("Current Name");
        user.setPosition("Current Position");
        user.setDepartment("Current Department");
        user.setBio("Current bio");
        user.setAvatarUrl(null);

        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "   ",
                "",
                "   ",
                ""
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));

        UserResponseDto result = userService.updateCurrentUserProfile(request);

        assertThat(result.displayName()).isEmpty();
        assertThat(result.position()).isEmpty();
        assertThat(result.department()).isEmpty();
        assertThat(result.bio()).isEmpty();
        assertThat(result.email()).isEqualTo("risen.cumin.22@icloud.com");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationStatus()).isEqualTo(ConfirmationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should throw exception when updating profile for missing current user")
    void shouldThrowExceptionWhenUpdatingProfileForMissingCurrentUser() {
        UserProfileUpdateDto request = new UserProfileUpdateDto(
                "New Name",
                "Backend Developer",
                "Platform",
                "Updated bio"
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateCurrentUserProfile(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should update current user avatar")
    void shouldUpdateCurrentUserAvatar() throws Exception {
        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser("encoded-password", "risen.cumin.22@icloud.com");
        ReflectionTestUtils.setField(user, "id", userId);
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);
        user.setDisplayName("Risen Cumin");
        user.setPosition("Backend Developer");
        user.setDepartment("Platform");
        user.setBio("Works on avatar upload");
        user.setAvatarUrl(null);

        InputStream avatarStream = new ByteArrayInputStream("avatar-content".getBytes());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.png");
        when(multipartFile.getInputStream()).thenReturn(avatarStream);
        when(awsS3Configuration.getPublicBaseUrl())
                .thenReturn("https://test-bucket.fra1.digitaloceanspaces.com");
        when(fileService.uploadFileAsync(
                anyString(),
                any(InputStream.class),
                anyMap(),
                eq("image/png"),
                eq(1024L),
                eq(true)
        )).thenReturn(CompletableFuture.completedFuture(true));

        UserResponseDto result = userService.updateCurrentUserAvatar(multipartFile);

        assertThat(result.avatarUrl())
                .startsWith("https://test-bucket.fra1.digitaloceanspaces.com/avatars/" + userId + "/")
                .endsWith(".png");
        assertThat(user.getAvatarUrl()).isEqualTo(result.avatarUrl());
        assertThat(result.email()).isEqualTo("risen.cumin.22@icloud.com");
        assertThat(result.displayName()).isEqualTo("Risen Cumin");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationStatus()).isEqualTo(ConfirmationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should reject empty avatar file")
    void shouldRejectEmptyAvatarFile() {
        when(multipartFile.isEmpty()).thenReturn(true);

        assertThatThrownBy(() -> userService.updateCurrentUserAvatar(multipartFile))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Avatar file cannot be empty or null");
                });
    }

    @Test
    @DisplayName("Should reject avatar file that exceeds max size")
    void shouldRejectAvatarFileThatExceedsMaxSize() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(UserValidationConstants.AVATAR_MAX_FILE_SIZE_BYTES + 1);

        assertThatThrownBy(() -> userService.updateCurrentUserAvatar(multipartFile))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Avatar file size is too large");
                });
    }

    @Test
    @DisplayName("Should reject avatar file with invalid content type")
    void shouldRejectAvatarFileWithInvalidContentType() {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("text/plain");

        assertThatThrownBy(() -> userService.updateCurrentUserAvatar(multipartFile))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Avatar file content type is invalid");
                });
    }

    @Test
    @DisplayName("Should throw exception when avatar upload fails")
    void shouldThrowExceptionWhenAvatarUploadFails() throws Exception {
        UUID userId = UUID.randomUUID();
        AppUser user = new AppUser("encoded-password", "risen.cumin.22@icloud.com");
        ReflectionTestUtils.setField(user, "id", userId);
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);

        InputStream avatarStream = new ByteArrayInputStream("avatar-content".getBytes());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("image/png");
        when(multipartFile.getOriginalFilename()).thenReturn("avatar.png");
        when(multipartFile.getInputStream()).thenReturn(avatarStream);
        when(fileService.uploadFileAsync(
                anyString(),
                any(InputStream.class),
                anyMap(),
                eq("image/png"),
                eq(1024L),
                eq(true)
        )).thenReturn(CompletableFuture.completedFuture(false));

        assertThatThrownBy(() -> userService.updateCurrentUserAvatar(multipartFile))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getMessage()).isEqualTo("Failed to upload avatar");
                });
    }

    @Test
    @DisplayName("Should change current user password")
    void shouldChangeCurrentUserPassword() {
        AppUser user = new AppUser("encoded-old-password", "risen.cumin.22@icloud.com");
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "OldPassword123!",
                "NewPassword123!"
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPassword123!", "encoded-old-password"))
                .thenReturn(true);
        when(passwordEncoder.encode("NewPassword123!"))
                .thenReturn("encoded-new-password");

        userService.changeCurrentUserPassword(request);

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        verify(passwordEncoder).matches("OldPassword123!", "encoded-old-password");
        verify(passwordEncoder).encode("NewPassword123!");
    }

    @Test
    @DisplayName("Should throw bad request when current password is incorrect")
    void shouldThrowBadRequestWhenCurrentPasswordIsIncorrect() {
        AppUser user = new AppUser("encoded-old-password", "risen.cumin.22@icloud.com");
        user.setRole(Role.ROLE_USER);
        user.setConfirmationStatus(ConfirmationStatus.CONFIRMED);

        ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                "WrongPassword123!",
                "NewPassword123!"
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("risen.cumin.22@icloud.com", null)
        );

        when(repository.findByEmailIgnoreCase("risen.cumin.22@icloud.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword123!", "encoded-old-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> userService.changeCurrentUserPassword(request))
                .isInstanceOfSatisfying(RestApiException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(ex.getMessage()).isEqualTo("Current password is incorrect");
                });

        assertThat(user.getPassword()).isEqualTo("encoded-old-password");
        verify(passwordEncoder, never()).encode("NewPassword123!");
    }
}
