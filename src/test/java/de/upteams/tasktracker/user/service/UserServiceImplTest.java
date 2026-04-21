package de.upteams.tasktracker.user.service;

import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.entity.Role;
import de.upteams.tasktracker.user.exception.UserNotFoundException;
import de.upteams.tasktracker.user.persistence.UserRepository;
import de.upteams.tasktracker.user.service.impl.UserServiceImpl;
import de.upteams.tasktracker.user.util.AppUserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl tests")
class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private AppUserMapper mappingService;

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
}
