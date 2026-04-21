package de.upteams.tasktracker.user.constants;

public final class UserValidationConstants {

    private UserValidationConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\p{Punct}])[A-Za-z\\d\\p{Punct}]{8,}$";
}
