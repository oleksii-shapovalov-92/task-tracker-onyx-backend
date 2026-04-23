package de.upteams.tasktracker.mail.password.reset.token;

public class PasswordResetTokenInvalidException extends RuntimeException {
    public PasswordResetTokenInvalidException() {
        super("Password reset token is invalid");
    }
}