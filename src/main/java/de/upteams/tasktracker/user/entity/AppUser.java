package de.upteams.tasktracker.user.entity;

import de.upteams.tasktracker.user.validation.ValidEmail;
import de.upteams.tasktracker.utils.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.ColumnDefault;

import static de.upteams.tasktracker.user.constants.UserValidationConstats.PASSWORD_REGEX;

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
    private Role role;

    public AppUser(String password, String email) {
        this.password = password;
        this.email = email;
        role = Role.ROLE_USER;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id=" + id +
                ", confirmationStatus=" + confirmationStatus +
                ", password='" + (StringUtils.isBlank(password) ? "null" : "*hidden*") + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                '}';
    }
}
