package de.upteams.tasktracker.user.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailValidator unit tests")
public class EmailValidatorTest {

    private final EmailValidator validator = new EmailValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "test@example.com",
            "user.name@test-domain.org",
            "my_user123@test.co",
            "aa@bb.cc"
    })
    @DisplayName("Should accept valid emails")
    void shouldReturnTrueForValidEmails(String email) {
        // Verify that valid emails pass the validation
        boolean result = validator.isValid(email, null);
        assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plaintext",
            "@nodomain.com",
            "noatsign.com",
            "test@.com",
            "te st@domain.com",
            "test@@domain.com",
            "@@",
            "test@domain.c",
            "t@d.c",
            "тест@domain.com"
    })
    @DisplayName("Should reject invalid emails")
    void shouldReturnFalseForInvalidEmails(String email) {
        // Verify that invalid emails are rejected by the validator
        boolean result = validator.isValid(email, null);
        assertThat(result).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("Should return true for blank strings because it is handled by @NotBlank")
    void shouldReturnTrueForBlankValuesBecauseNotBlankHandlesThem(String email) {
        // Blank values are valid here. @NotBlank handles them.
        boolean result = validator.isValid(email, null);
        assertThat(result).isTrue();
    }
}
