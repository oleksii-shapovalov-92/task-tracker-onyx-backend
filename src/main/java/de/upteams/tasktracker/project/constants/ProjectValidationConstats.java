package de.upteams.tasktracker.project.constants;

public final class ProjectValidationConstats {

    private ProjectValidationConstats() {
        throw new IllegalStateException("Utility class");
    }

    public static final String TITLE_REGEX = "^[A-Z][A-Za-z0-9 .&'()\\-]*[A-Za-z0-9)]$";

    public static final String DESCRIPTION_REGEX =
            "^[A-Z0-9][A-Za-z0-9\\s.,:;!?%&$'\"()\\-]*[A-Za-z0-9.!?)]$";

    public static final int TITLE_MIN_LENGTH = 3;
    public static final int TITLE_MAX_LENGTH = 100;

    public static final int DESCRIPTION_MIN_LENGTH = 10;
    public static final int DESCRIPTION_MAX_LENGTH = 500;
}