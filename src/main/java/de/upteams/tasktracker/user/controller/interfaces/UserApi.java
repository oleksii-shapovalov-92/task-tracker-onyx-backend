package de.upteams.tasktracker.user.controller.interfaces;

import de.upteams.tasktracker.user.dto.request.UserProfileUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.dto.request.ChangePasswordRequestDto;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST mappings for user operations.
 * Implementation classes should implement this interface.
 */
@RequestMapping("/api/v1/users")
public interface UserApi extends UserApiSwaggerDoc {

    @Override
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    List<UserResponseDto> getAll();


    @Override
    @GetMapping("/me")
    ResponseEntity<UserResponseDto> getCurrentUser();

    @Override
    @PatchMapping("/me")
    ResponseEntity<UserResponseDto> updateCurrentUserProfile(
            @Valid @RequestBody UserProfileUpdateDto userProfileUpdateDto);

    @Override
    @PatchMapping("/me/password")
    ResponseEntity<Void> changeCurrentUserPassword(
            @Valid @RequestBody ChangePasswordRequestDto request
    );

    @Override
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<UserResponseDto> updateCurrentUserAvatar(
            @Parameter(
                    description = "Avatar image file. Supported types: JPEG, PNG, WEBP. Max size: 5 MB",
                    required = true,
                    schema = @Schema(type = "string", format = "binary")
            )
            @RequestPart("file") MultipartFile file
    );
}
