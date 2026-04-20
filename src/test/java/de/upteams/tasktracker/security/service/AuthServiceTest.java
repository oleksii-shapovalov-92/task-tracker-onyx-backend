package de.upteams.tasktracker.security.service;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.security.dto.LoginRequest;
import de.upteams.tasktracker.security.entities.TokenResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_shouldReturnTokens_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getName()).thenReturn("user@example.com");
        when(jwtTokenService.generateAccessToken("user@example.com")).thenReturn("access-token");
        when(jwtTokenService.generateRefreshToken("user@example.com")).thenReturn("refresh-token");

        TokenResponseDto response = authService.login(request);

        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
    }

    @Test
    void login_shouldReturnGeneric401Message_whenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("nonexistent@example.com", "Test12345");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("User not found: nonexistent@example.com"));

        RestApiException ex = assertThrows(RestApiException.class, () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
        assertEquals("Invalid username or password.", ex.getMessage());

        verify(jwtTokenService, never()).generateAccessToken(anyString());
        verify(jwtTokenService, never()).generateRefreshToken(anyString());
    }

    @Test
    void login_shouldReturn403_whenUserIsDisabled() {
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("disabled"));

        RestApiException ex = assertThrows(RestApiException.class, () -> authService.login(request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        assertEquals("User account is not active. Please confirm your email.", ex.getMessage());
    }

    @Test
    void login_shouldReturn403_whenUserIsLocked() {
        LoginRequest request = new LoginRequest("user@example.com", "Password123!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("locked"));

        RestApiException ex = assertThrows(RestApiException.class, () -> authService.login(request));

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        assertEquals("User account is locked.", ex.getMessage());
    }
}
