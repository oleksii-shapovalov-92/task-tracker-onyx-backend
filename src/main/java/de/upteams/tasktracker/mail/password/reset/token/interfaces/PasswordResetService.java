package de.upteams.tasktracker.mail.password.reset.token.interfaces;

import de.upteams.tasktracker.mail.password.reset.token.PasswordResetToken;
import de.upteams.tasktracker.user.entity.AppUser;

public interface PasswordResetService {

    String generateResetToken(AppUser user);

    PasswordResetToken getResetTokenIfValidOrThrow(String token);

    void markAsUsed(PasswordResetToken token);
}