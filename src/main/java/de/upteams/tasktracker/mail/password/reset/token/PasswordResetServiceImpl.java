package de.upteams.tasktracker.mail.password.reset.token;

import de.upteams.tasktracker.mail.password.reset.token.interfaces.PasswordResetService;
import de.upteams.tasktracker.mail.password.reset.token.interfaces.PasswordResetTokenRepository;
import de.upteams.tasktracker.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    @Value("${password-reset.expiration.hours:1}")
    private int expirationHours;

    private final PasswordResetTokenRepository repository;

    @Override
    public String generateResetToken(AppUser user) {
        return generatePasswordResetToken(user).getId().toString();
    }

    public PasswordResetToken generatePasswordResetToken(AppUser user) {

        Optional<PasswordResetToken> optionalToken = repository.findByUser(user);

        if (optionalToken.isPresent()) {
            PasswordResetToken existingToken = optionalToken.get();
            existingToken.setExpiration(LocalDateTime.now().plusHours(expirationHours));
            existingToken.setUsed(false);
            return repository.save(existingToken);
        }

        PasswordResetToken token = new PasswordResetToken(
                LocalDateTime.now().plusHours(expirationHours),
                false,
                user
        );

        return repository.save(token);
    }

    @Override
    public PasswordResetToken getResetTokenIfValidOrThrow(String token) {
        UUID tokenId;

        try {
            tokenId = UUID.fromString(token);
        } catch (IllegalArgumentException ex) {
            throw new PasswordResetTokenInvalidException();
        }

        PasswordResetToken tokenEntity = repository.findById(tokenId)
                .orElseThrow(PasswordResetTokenInvalidException::new);

        if  (Boolean.TRUE.equals(tokenEntity.getUsed())) {
            throw new PasswordResetTokenAlreadyUsedException();
        }

        if (tokenEntity.getExpiration().isBefore(LocalDateTime.now())) {
            throw new PasswordResetTokenExpiredException();
        }

        return tokenEntity;
    }

    @Override
    public void markAsUsed(PasswordResetToken token) {
        token.setUsed(true);
        repository.save(token);
    }
}