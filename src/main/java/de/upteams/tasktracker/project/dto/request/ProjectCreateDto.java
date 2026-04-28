package de.upteams.tasktracker.project.dto.request;

import de.upteams.tasktracker.project.constants.ProjectValidationConstats;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

/**
 * Project DTO
 */
@Schema(description = "Data Transfer Object for Project entity")
public record ProjectCreateDto(
        @Schema(
                description = "Title of the Project",
                example = "New Website Development"
        )
        @NotBlank(message = "Project title is required")
        @Length(
                min = ProjectValidationConstats.NAME_MIN_LENGTH,
                max = ProjectValidationConstats.NAME_MAX_LENGTH,
                message = "Project title must be between 3 and 155 characters long"
        )
        @Pattern(
                regexp = ProjectValidationConstats.NAME_REGEX,
                message = "Project title must start with a capital letter and may contain only letters, digits and spaces"
        )
        String title,

        @Schema(
                description = "Detailed description of the Project",
                example = "A Project to develop a new company website"
        )
        @NotBlank(message = "Project description is required")
        @Length(
                min = ProjectValidationConstats.DESCRIPTION_MIN_LENGTH,
                max = ProjectValidationConstats.DESCRIPTION_MAX_LENGTH,
                message = "Project description must be between 3 and 155 characters long"
        )
        @Pattern(
                regexp = ProjectValidationConstats.DESCRIPTION_REGEX,
                message = "Project description must start with a capital letter and may contain only letters, digits, spaces, and , . % : ? & ! $ ; * ( )"
        )
        String description) {

}
