package de.upteams.tasktracker.user.entity;

import de.upteams.tasktracker.user.validation.ValidEmail;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.validator.constraints.URL;

import static de.upteams.tasktracker.user.constants.UserValidationConstants.PASSWORD_REGEX;

import de.upteams.tasktracker.user.constants.UserValidationConstants;


/**
 * Application User entity
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    @NotBlank(message = "{user.password.notBlank}")
    @Pattern(
            regexp = PASSWORD_REGEX,
            message = "{user.password.invalid}"
    )
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank(message = "{user.email.notBlank}")
    @ValidEmail
    @Column(
            name = "email",
            unique = true,
            nullable = false,
            columnDefinition = "VARCHAR(255) COLLATE ascii_bin"
    )
    private String email;

    @NotNull(message = "{field.notNull}")
    @Column(name = "confirm_status", nullable = false)
    @ColumnDefault("'UNCONFIRMED'")
    @Enumerated(EnumType.STRING)
    private ConfirmationStatus confirmationStatus = ConfirmationStatus.UNCONFIRMED;

    @NotNull(message = "{field.notNull}")
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;


    @Size(max = UserValidationConstants.DISPLAY_NAME_MAX_LENGTH,
            message = "{user.displayName.size}")
    @Column(
            name = "display_name",
            nullable = false)
    private String displayName = "";

    @Size(max = UserValidationConstants.POSITION_MAX_LENGTH,
            message = "{user.position.size}")
    @Column(
            name = "position",
            nullable = false)
    private String position = "";

    @Size(max = UserValidationConstants.DEPARTMENT_MAX_LENGTH,
            message = "{user.department.size}")
    @Column(
            name = "department",
            nullable = false)
    private String department = "";


    @Size(max = UserValidationConstants.AVATAR_URL_MAX_LENGTH,
            message = "{user.avatarUrl.size}")
    @URL(message = "{user.avatarUrl.invalid}")
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Size(max = UserValidationConstants.BIO_MAX_LENGTH,
            message = "{user.bio.size}")
    @Column(
            name = "bio",
            nullable = false)
    private String bio = "";


    public AppUser(String password, String email) {
        this.password = password;
        this.email = email;
    }

    public AppUser(String email, Role role, String displayName, String position,
                   String department, String avatarUrl, String bio) {
        this.email = email;
        this.role = role == null ? Role.ROLE_USER : role;
        this.displayName = StringUtils.defaultString(displayName);
        this.position = StringUtils.defaultString(position);
        this.department = StringUtils.defaultString(department);
        this.avatarUrl = avatarUrl;
        this.bio = StringUtils.defaultString(bio);
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", confirmationStatus=" + confirmationStatus +
                ", password='" + (StringUtils.isBlank(password) ? "null" : "*hidden*") + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", displayName='" + displayName + '\'' +
                ", position='" + position + '\'' +
                ", department='" + department + '\'' +
                '}';
    }
}
