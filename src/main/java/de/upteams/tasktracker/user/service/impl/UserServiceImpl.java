package de.upteams.tasktracker.user.service.impl;

import de.upteams.tasktracker.configuration.AwsS3Configuration;
import de.upteams.tasktracker.exception.handling.exceptions.common.RestApiException;
import de.upteams.tasktracker.files.uploading.FileService;
import de.upteams.tasktracker.user.constants.UserValidationConstants;
import de.upteams.tasktracker.user.dto.request.UserProfileUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.entity.AppUser;
import de.upteams.tasktracker.user.exception.UserNotFoundException;
import de.upteams.tasktracker.user.persistence.UserRepository;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.user.util.AppUserMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for various operations with Employees
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final AppUserMapper mappingService;
    private final FileService fileService;
    private final AwsS3Configuration awsS3Configuration;


    @Override
    public AppUser saveOrUpdate(final AppUser user) {
        return repository.save(user);
    }

    @Override
    @Transactional
    public Optional<AppUser> getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    @Override
    @Transactional
    public AppUser getByEmailOrThrow(String email) {
        return getByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    @Transactional
    public AppUser getByIdOrThrow(String id) {
        return repository
                .findById(UUID.fromString(id))
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public List<UserResponseDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mappingService::mapEntityToDto)
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        AppUser user = getByEmailOrThrow(email);
        return mapToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateCurrentUserProfile(UserProfileUpdateDto userProfileUpdateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        AppUser user = getByEmailOrThrow(email);

        if (userProfileUpdateDto.displayName() != null) {
            user.setDisplayName(userProfileUpdateDto.displayName().trim());
        }
        if (userProfileUpdateDto.position() != null) {
            user.setPosition(userProfileUpdateDto.position().trim());
        }
        if (userProfileUpdateDto.department() != null) {
            user.setDepartment(userProfileUpdateDto.department().trim());
        }
        if (userProfileUpdateDto.bio() != null) {
            user.setBio(userProfileUpdateDto.bio().trim());
        }
        return mapToUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateCurrentUserAvatar(MultipartFile file) {
        validateAvatarFile(file);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        AppUser user = getByEmailOrThrow(email);

        String objectKey = buildAvatarObjectKey(user, file);

        try {
            boolean uploaded = fileService.uploadFileAsync(
                    objectKey,
                    file.getInputStream(),
                    Map.of("userId", user.getId().toString()),
                    file.getContentType(),
                    file.getSize(),
                    true
            ).join();
            if (!uploaded) {
                throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload avatar");
            }
            user.setAvatarUrl(buildPublicAvatarUrl(objectKey));
            return mapToUserResponseDto(user);
        } catch (IOException e) {
            throw new RestApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read avatar file");
        }
    }


    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Avatar file cannot be empty or null");
        }
        if (file.getSize() > UserValidationConstants.AVATAR_MAX_FILE_SIZE_BYTES) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Avatar file size is too large");
        }
        if (!UserValidationConstants.ALLOWED_AVATAR_CONTENT_TYPES.contains(file.getContentType())) {
            throw new RestApiException(HttpStatus.BAD_REQUEST, "Avatar file content type is invalid");
        }
    }

    private String buildAvatarObjectKey(AppUser user, MultipartFile file) {
        String extension = extractExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + extension;

        return UserValidationConstants.AVATAR_OBJECT_KEY_PREFIX
                + "/" + user.getId()
                + "/" + fileName;
    }

    private String extractExtension(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            return "";
        }
        int extensionStartIndex = originalFileName.lastIndexOf('.');
        if (extensionStartIndex < 0 || extensionStartIndex == originalFileName.length() - 1) {
            return "";
        }
        return originalFileName.substring(extensionStartIndex);
    }

    private String buildPublicAvatarUrl(String objectKey) {
        String publicBaseUrl = awsS3Configuration.getPublicBaseUrl();
        if (publicBaseUrl.endsWith("/")) {
            return publicBaseUrl + objectKey;
        }
        return publicBaseUrl + "/" + objectKey;
    }


    private UserResponseDto mapToUserResponseDto(AppUser user) {
        return new UserResponseDto(
                user.getDisplayName(),
                user.getPosition(),
                user.getDepartment(),
                user.getAvatarUrl(),
                user.getBio(),
                user.getEmail(),
                user.getRole().name(),
                user.getConfirmationStatus()
        );
    }
}
