package de.upteams.tasktracker.user.controller.impl;

import de.upteams.tasktracker.user.controller.interfaces.RegisterControllerApi;
import de.upteams.tasktracker.user.dto.request.UserCreateDto;
import de.upteams.tasktracker.user.dto.response.UserConfirmationResponseDto;
import de.upteams.tasktracker.user.dto.response.UserCreateResponseDto;
import de.upteams.tasktracker.user.service.impl.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RegisterControllerImpl implements RegisterControllerApi {

    private final UserRegisterService service;

    @Override
    public UserCreateResponseDto register(UserCreateDto registerUser) {
        return service.register(registerUser);
    }

    @Override
    @GetMapping("/confirm/{code}")
    public UserConfirmationResponseDto confirmRegistration(@PathVariable String code) {
        return service.confirmRegistration(code);
    }
}
