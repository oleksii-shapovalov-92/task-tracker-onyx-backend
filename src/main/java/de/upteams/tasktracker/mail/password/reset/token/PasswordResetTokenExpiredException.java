package de.upteams.tasktracker.mail.password.reset.token;

public class PasswordResetTokenExpiredException extends RuntimeException {
    public PasswordResetTokenExpiredException() {
        super("Password reset token has expired");
    }
}