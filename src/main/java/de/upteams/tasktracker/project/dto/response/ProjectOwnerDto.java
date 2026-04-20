package de.upteams.tasktracker.project.dto.response;

import java.util.UUID;

public record ProjectOwnerDto(
        UUID id,
        String email
) {
}
