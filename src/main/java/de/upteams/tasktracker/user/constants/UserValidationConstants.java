package de.upteams.tasktracker.user.constants;

import java.util.Set;

public final class UserValidationConstants {

    private UserValidationConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String PASSWORD_REGEX =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\p{Punct}])[A-Za-z\\d\\p{Punct}]{8,}$";

    public static final int DISPLAY_NAME_MAX_LENGTH = 100;
    public static final int POSITION_MAX_LENGTH = 100;
    public static final int DEPARTMENT_MAX_LENGTH = 100;
    public static final int AVATAR_URL_MAX_LENGTH = 2048;
    public static final int BIO_MAX_LENGTH = 1000;
    public static final long AVATAR_MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024L;
    public static final String AVATAR_OBJECT_KEY_PREFIX = "avatars";
    public static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
}
