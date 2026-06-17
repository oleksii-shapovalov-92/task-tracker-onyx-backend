package de.upteams.tasktracker.task.constants;

public final class TaskValidationConstats {

    private TaskValidationConstats() {
        throw new IllegalStateException("Utility class");
    }

    public static final String TITLE_REGEX = "[A-Z][a-zA-Z0-9 ]{2,}";
    public static final String TITLE_MESSAGE =
            "Task title should be at least 3 character length and start with capital letter";

    public static final String DESCRIPTION_REGEX = "[A-Z][a-zA-Z0-9,.%:?&!$;*() ]{2,}";
    public static final String DESCRIPTION_MESSAGE =
            "Task description should be at least 3 character length and start with capital letter";
}