package de.upteams.tasktracker.mail.password.reset.token.interfaces;

import de.upteams.tasktracker.mail.password.reset.token.PasswordResetToken;
import de.upteams.tasktracker.user.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {

    Optional<PasswordResetToken> findByUser(AppUser user);
}