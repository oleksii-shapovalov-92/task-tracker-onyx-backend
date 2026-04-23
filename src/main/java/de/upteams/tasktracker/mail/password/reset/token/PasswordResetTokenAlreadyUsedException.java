package de.upteams.tasktracker.mail.password.reset.token;

public class PasswordResetTokenAlreadyUsedException extends RuntimeException {
    public PasswordResetTokenAlreadyUsedException() {
        super("Password reset token has already been used");
    }
}