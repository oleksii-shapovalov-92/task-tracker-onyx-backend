package de.upteams.tasktracker.user.controller.impl;

import de.upteams.tasktracker.user.controller.interfaces.RegisterControllerApi;
import de.upteams.tasktracker.user.dto.request.UserCreateDto;
import de.upteams.tasktracker.user.dto.response.UserCreateResponseDto;
import de.upteams.tasktracker.user.service.impl.UserRegisterService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class RegisterControllerImpl implements RegisterControllerApi {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private final UserRegisterService service;

    @Override
    public UserCreateResponseDto register(UserCreateDto registerUser) {
        return service.register(registerUser);
    }

    @Override
    @GetMapping("/confirm/{code}")
    public ResponseEntity<Void> confirmRegistration(@PathVariable String code) {

        service.confirmRegistration(code);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(frontendUrl + "/login"))
                .build();
    }
}