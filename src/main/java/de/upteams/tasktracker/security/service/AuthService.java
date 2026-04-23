package de.upteams.tasktracker.security.service;

import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.mail.EmailService;
import de.upteams.tasktracker.mail.password.reset.token.PasswordResetToken;
import de.upteams.tasktracker.mail.password.reset.token.interfaces.PasswordResetService;
import de.upteams.tasktracker.security.dto.ForgotPasswordRequestDto;
import de.upteams.tasktracker.security.dto.LoginRequest;
import de.upteams.tasktracker.security.dto.ResetPasswordRequestDto;
import de.upteams.tasktracker.security.entities.TokenResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    public TokenResponseDto login(LoginRequest loginRequest) {
        String userEmail = loginRequest.email();

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userEmail, loginRequest.password())
            );
        } catch (DisabledException ex) {
            log.warn("Login attempt for inactive account: {}", userEmail, ex);
            throw new RestApiException(
                    HttpStatus.FORBIDDEN,
                    "User account is not active. Please confirm your email."
            );
        } catch (LockedException ex) {
            log.warn("Login attempt for locked account: {}", userEmail, ex);
            throw new RestApiException(
                    HttpStatus.FORBIDDEN,
                    "User account is locked."
            );
        } catch (BadCredentialsException ex) {
            log.warn("Bad credentials for user: {}", userEmail, ex);
            throw new RestApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password."
            );
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for user: {}", userEmail, ex);
            throw new RestApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password."
            );
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String authenticatedUserEmail = authentication.getName();
        String accessToken = jwtTokenService.generateAccessToken(authenticatedUserEmail);
        String refreshToken = jwtTokenService.generateRefreshToken(authenticatedUserEmail);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public String refreshAccessToken(String refreshToken) {
        if (jwtTokenService.validateToken(refreshToken, JwtTokenService.TokenType.REFRESH)) {
            String username = jwtTokenService.getUsernameFromToken(refreshToken, JwtTokenService.TokenType.REFRESH);
            return jwtTokenService.generateAccessToken(username);
        }
        throw new RestApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    public void forgotPassword(ForgotPasswordRequestDto request) {
        String normalizedEmail = request.email().toLowerCase().trim();

        AppUser user = userService.getByEmail(normalizedEmail)
                .orElseThrow(() -> new RestApiException(
                        HttpStatus.NOT_FOUND,
                        "User with this email was not found"
                ));

        String resetToken = passwordResetService.generateResetToken(user);
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        PasswordResetToken resetToken =
                passwordResetService.getResetTokenIfValidOrThrow(request.token());

        AppUser user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));

        userService.saveOrUpdate(user);

        passwordResetService.markAsUsed(resetToken);
    }
}