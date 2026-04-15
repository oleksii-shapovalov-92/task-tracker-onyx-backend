package de.upteams.entity;

import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.entity.ConfirmationStatus;
import de.upteams.tasktracker.user.entity.Role;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AppUser password validation test")
public class AppUserValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation when password matches all requirements")
    void shouldPassValidationWhenPasswordIsValid() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("ValidPassword123!");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .noneMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when password is blank")
    void shouldFailValidationWhenPasswordIsBlank() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when password is shorter then 8 characters")
    void shouldFailValidationWhenPasswordIsTooShort() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("Val1!");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when password has no uppercase letter")
    void shouldFailValidationWhenPasswordHasNoUppercaseLetter() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("validpassword123!");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when password has no lowercase letter")
    void shouldFailValidationWhenPasswordHasNoLowercaseLetter() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("VALIDPASSWORD123!");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when password has no digit")
    void shouldFailValidationWhenPasswordHasNoDigit() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("ValidPassword!");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when password has no special character")
    void shouldFailValidationWhenPasswordHasNoSpecialCharacter() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("ValidPassword123");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when password contains non-latin letters")
    void shouldFailValidationWhenPasswordContainsNonLatinLetters() {
        AppUser appUser = buildValidUser();
        appUser.setPassword("ValidPassword123!Ы");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "password".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should pass validation when email is valid")
    void shouldPassValidationWhenEmailIsValid() {
        AppUser appUser = buildValidUser();
        appUser.setEmail("test@example.com");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .noneMatch(violation -> "email".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when email is invalid")
    void shouldFailValidationWhenEmailIsInvalid() {
        AppUser appUser = buildValidUser();
        appUser.setEmail("plaintext");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "email".equals(violation.getPropertyPath().toString()));
    }

    @Test
    @DisplayName("Should fail validation when email is blank")
    void shouldFailValidationWhenEmailIsBlank() {
        AppUser appUser = buildValidUser();
        appUser.setEmail("   ");

        Set<ConstraintViolation<AppUser>> violations = validator.validate(appUser);

        assertThat(violations)
                .anyMatch(violation -> "email".equals(violation.getPropertyPath().toString()));
    }

    private AppUser buildValidUser() {
        AppUser appUser = new AppUser();
        appUser.setEmail("test@example.com");
        appUser.setPassword("ValidPassword123!");
        appUser.setRole(Role.ROLE_USER);
        appUser.setConfirmationStatus(ConfirmationStatus.UNCONFIRMED);
        return appUser;
    }
}
