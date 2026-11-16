package ru.skillbox.socialnetwork.auth1.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skillbox.socialnetwork.auth1.dto.AuthenticateRq;
import ru.skillbox.socialnetwork.auth1.dto.RefreshRequest;
import ru.skillbox.socialnetwork.auth1.dto.RegistrationDto;
import ru.skillbox.socialnetwork.auth1.dto.TokenResponse;
import ru.skillbox.socialnetwork.auth1.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegistrationDto registrationDto
    ) {
        authService.registerNewUser(registrationDto);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshRequest request
            ){
      TokenResponse body = authService.refreshToken(request);
        return ResponseEntity.ok(body);
    }
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody AuthenticateRq authenticateRq
    ){
        TokenResponse body = authService.login(authenticateRq);
        return ResponseEntity.ok(body);
    }
}
