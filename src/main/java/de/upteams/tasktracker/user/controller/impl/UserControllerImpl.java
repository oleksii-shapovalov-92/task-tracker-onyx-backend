package de.upteams.tasktracker.user.controller.impl;

import de.upteams.tasktracker.user.controller.interfaces.UserApi;
import de.upteams.tasktracker.user.dto.request.UserProfileUpdateDto;
import de.upteams.tasktracker.user.dto.response.UserResponseDto;
import de.upteams.tasktracker.user.service.UserService;
import de.upteams.tasktracker.user.dto.request.ChangePasswordRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller that receives http-requests for various operations with Employees
 */
@RestController
@RequiredArgsConstructor
public class UserControllerImpl implements UserApi {

    /**
     * Service for various operations with Employees
     */
    private final UserService service;

    @Override
    public List<UserResponseDto> getAll() {
        return service.getAll();
    }

    @Override
    public ResponseEntity<UserResponseDto> getCurrentUser() {
        return ResponseEntity.ok(service.getCurrentUser());
    }

    @Override
    public ResponseEntity<UserResponseDto> updateCurrentUserProfile(
            UserProfileUpdateDto userProfileUpdateDto) {
        return ResponseEntity.ok(service.updateCurrentUserProfile(userProfileUpdateDto));
    }

    @Override
    public ResponseEntity<UserResponseDto> updateCurrentUserAvatar(MultipartFile file) {
        return ResponseEntity.ok(service.updateCurrentUserAvatar(file));
    }

    @Override
    public ResponseEntity<Void> changeCurrentUserPassword(ChangePasswordRequestDto request) {
        service.changeCurrentUserPassword(request);
        return ResponseEntity.noContent().build();
    }
}
