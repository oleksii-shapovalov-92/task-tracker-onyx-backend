package de.upteams.tasktracker.security.handler;

import de.upteams.tasktracker.mail.password.reset.token.PasswordResetTokenAlreadyUsedException;
import de.upteams.tasktracker.mail.password.reset.token.PasswordResetTokenExpiredException;
import de.upteams.tasktracker.mail.password.reset.token.PasswordResetTokenInvalidException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class PasswordResetExceptionHandler {

    @ExceptionHandler(PasswordResetTokenInvalidException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenInvalid(
            PasswordResetTokenInvalidException ignored,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        Instant.now().toString(),
                        status.value(),
                        status.getReasonPhrase(),
                        "This password reset link is invalid.",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(PasswordResetTokenAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenAlreadyUsed(
            PasswordResetTokenAlreadyUsedException ignored,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.GONE;

        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        Instant.now().toString(),
                        status.value(),
                        status.getReasonPhrase(),
                        "This password reset link has already been used. Please request a new one.",
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(PasswordResetTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handlePasswordResetTokenExpired(
            PasswordResetTokenExpiredException ignored,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.GONE;

        return ResponseEntity.status(status).body(
                new ErrorResponse(
                        Instant.now().toString(),
                        status.value(),
                        status.getReasonPhrase(),
                        "This password reset link has expired. Please request a new one.",
                        request.getRequestURI()
                )
        );
    }
}