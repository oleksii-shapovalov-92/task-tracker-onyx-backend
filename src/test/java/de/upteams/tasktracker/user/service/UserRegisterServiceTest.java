package de.upteams.tasktracker.user.service;

import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.mail.confirmation.code.interfaces.ConfirmationService;
import de.upteams.tasktracker.user.dto.request.UserCreateDto;
import de.upteams.tasktracker.user.dto.response.UserCreateResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.entity.Role;
import de.upteams.tasktracker.user.exception.UserAlreadyExistException;
import de.upteams.tasktracker.user.service.impl.UserRegisterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRegisterService tests")
class UserRegisterServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private ConfirmationService confirmationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRegisterService userRegisterService;

    @Test
    @DisplayName("Should throw 409 exception when confirmed user already exists")
    void shouldThrowConflictWhenConfirmedUserAlreadyExists() {
        UserCreateDto dto = new UserCreateDto("serdar_test@upteams.de", "Test12345!");

        UUID existingUserId = UUID.randomUUID();

        AppUser existingUser = new AppUser("encoded-password", "serdar_test@upteams.de");
        ReflectionTestUtils.setField(existingUser, "id", existingUserId);
        existingUser.setRole(Role.ROLE_USER);
        existingUser.setConfirmationStatus(ConfirmationStatus.CONFIRMED);

        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");
        when(userService.getByEmail("serdar_test@upteams.de"))
                .thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userRegisterService.register(dto))
                .isInstanceOf(UserAlreadyExistException.class)
                .hasMessage("User already exists");

        verify(passwordEncoder).encode(dto.password());
        verify(userService).getByEmail("serdar_test@upteams.de");
        verify(userService, never()).saveOrUpdate(any());
        verifyNoInteractions(emailService, confirmationService);
    }

    @Test
    @DisplayName("Should resend confirmation when unconfirmed user already exists")
    void shouldResendConfirmationWhenUnconfirmedUserAlreadyExists() {
        UserCreateDto dto = new UserCreateDto("serdar_test@upteams.de", "Test12345!");

        UUID existingUserId = UUID.randomUUID();

        AppUser existingUser = new AppUser("encoded-password", "serdar_test@upteams.de");
        ReflectionTestUtils.setField(existingUser, "id", existingUserId);
        existingUser.setRole(Role.ROLE_USER);
        existingUser.setConfirmationStatus(ConfirmationStatus.UNCONFIRMED);

        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");
        when(userService.getByEmail("serdar_test@upteams.de"))
                .thenReturn(Optional.of(existingUser));
        when(confirmationService.regenerateCode(existingUser)).thenReturn("new-confirmation-code");

        UserCreateResponseDto result = userRegisterService.register(dto);

        assertThat(result.id()).isEqualTo(existingUserId.toString());
        assertThat(result.email()).isEqualTo("serdar_test@upteams.de");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationResent()).isTrue();

        verify(passwordEncoder).encode(dto.password());
        verify(userService).getByEmail("serdar_test@upteams.de");
        verify(confirmationService).regenerateCode(existingUser);
        verify(emailService).sendConfirmationEmail("serdar_test@upteams.de", "new-confirmation-code");
        verify(userService, never()).saveOrUpdate(any());
    }

    @Test
    @DisplayName("Should register new user when email does not exist")
    void shouldRegisterNewUserWhenEmailDoesNotExist() {
        UserCreateDto dto = new UserCreateDto("new_user@upteams.de", "Test12345!");

        UUID savedUserId = UUID.randomUUID();

        when(passwordEncoder.encode(dto.password())).thenReturn("encoded-password");
        when(userService.getByEmail("new_user@upteams.de")).thenReturn(Optional.empty());

        AppUser savedUser = new AppUser("encoded-password", "new_user@upteams.de");
        ReflectionTestUtils.setField(savedUser, "id", savedUserId);
        savedUser.setRole(Role.ROLE_USER);

        when(userService.saveOrUpdate(any(AppUser.class))).thenReturn(savedUser);
        when(confirmationService.generateConfirmationCode(savedUser)).thenReturn("generated-code");

        UserCreateResponseDto result = userRegisterService.register(dto);

        assertThat(result.id()).isEqualTo(savedUserId.toString());
        assertThat(result.email()).isEqualTo("new_user@upteams.de");
        assertThat(result.role()).isEqualTo("ROLE_USER");
        assertThat(result.confirmationResent()).isFalse();

        verify(passwordEncoder).encode(dto.password());
        verify(userService).getByEmail("new_user@upteams.de");
        verify(userService).saveOrUpdate(any(AppUser.class));
        verify(confirmationService).generateConfirmationCode(savedUser);
        verify(emailService).sendConfirmationEmail("new_user@upteams.de", "generated-code");
    }
}