package com.example.ledgerpro.controller;

import com.example.ledgerpro.auth.AuthContextHolder;
import com.example.ledgerpro.auth.AuthPrincipal;
import com.example.ledgerpro.auth.AuthService;
import com.example.ledgerpro.dto.AuthLoginRequest;
import com.example.ledgerpro.dto.AuthLoginResponse;
import com.example.ledgerpro.dto.CurrentUserResponse;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthLoginResponse login(@Valid @RequestBody AuthLoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        AuthPrincipal principal = authService.requirePrincipal(token);

        AuthLoginResponse response = new AuthLoginResponse();
        response.setToken(token);
        response.setUserId(principal.getUserId());
        response.setUsername(principal.getUsername());
        response.setDisplayName(principal.getDisplayName());
        return response;
    }

    @GetMapping("/me")
    public CurrentUserResponse me() {
        AuthPrincipal principal = AuthContextHolder.get();
        CurrentUserResponse response = new CurrentUserResponse();
        response.setUserId(principal.getUserId());
        response.setUsername(principal.getUsername());
        response.setDisplayName(principal.getDisplayName());
        return response;
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            authService.logout(authorization.substring(7));
        }
    }
}
