package de.upteams.tasktracker.project.entity;

import de.upteams.tasktracker.collaborator.entity.Collaborator;
import de.upteams.tasktracker.project.constants.ProjectValidationConstats;
import de.upteams.tasktracker.task.entity.Task;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

import static de.upteams.tasktracker.utils.EntityUtil.getIdForToString;
import static de.upteams.tasktracker.utils.EntityUtil.getIdsForToString;

/**
 * Project entity
 */
@Entity
@Table(name = "project")
@NoArgsConstructor
@Getter
@Setter
public class Project extends BaseEntity {

    @Column(name = "title", nullable = false, length = ProjectValidationConstats.TITLE_MAX_LENGTH)
    @NotBlank(message = "Project title is required")
    @Size(
            min = ProjectValidationConstats.TITLE_MIN_LENGTH,
            max = ProjectValidationConstats.TITLE_MAX_LENGTH,
            message = "Project title must be between 3 and 100 characters long"
    )
    @Pattern(
            regexp = ProjectValidationConstats.TITLE_REGEX,
            message = "Project title must start with an uppercase letter and may contain only letters, digits, spaces, dots, ampersands, apostrophes, parentheses and hyphens"
    )
    private String title;

    @Column(name = "description", nullable = false, length = ProjectValidationConstats.DESCRIPTION_MAX_LENGTH)
    @NotBlank(message = "Project description is required")
    @Size(
            min = ProjectValidationConstats.DESCRIPTION_MIN_LENGTH,
            max = ProjectValidationConstats.DESCRIPTION_MAX_LENGTH,
            message = "Project description must be between 10 and 500 characters long"
    )
    @Pattern(
            regexp = ProjectValidationConstats.DESCRIPTION_REGEX,
            message = "Project description must start with an uppercase letter or digit and may contain only letters, digits, spaces and common punctuation"
    )
    private String description;

    @ManyToOne
    @NotNull
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Collaborator> projectTeam = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private final Set<Task> tasks = new HashSet<>();

    public Project(String title, String description, AppUser owner) {
        this.title = title;
        this.description = description;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + getId() +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", authorId=" + getIdForToString(owner) +
                ", tasksIds=" + getIdsForToString(tasks) +
                '}';
    }
}
