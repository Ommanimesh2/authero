package com.authero.authserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authero.authserver.dto.LoginDto;
import com.authero.authserver.dto.LoginResponse;
import com.authero.authserver.dto.SignupDto;
import com.authero.authserver.dto.github.SignUpWithGithubDto;
import com.authero.authserver.dto.google.SignUpWithGoogleDto;
import com.authero.authserver.models.User;
import com.authero.authserver.service.JwtService;
import com.authero.authserver.service.UserService;

import jakarta.validation.Valid;

@RequestMapping("/auth")
@RestController
public class AuthController {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody @Valid SignupDto signupDto) {
        User registeredUser = userService.signup(signupDto);

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody @Valid LoginDto loginDto) {
        User authenticatedUser = userService.login(loginDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signUpWithGithub")
    public ResponseEntity<LoginResponse> signUpWithGithub(@RequestBody SignUpWithGithubDto signUpWithGithubDto) {
        User authenticatedUser = userService.signUpWithGithub(signUpWithGithubDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/signUpWithGoogle")
    public ResponseEntity<LoginResponse> signUpWithGoogle(@RequestBody SignUpWithGoogleDto signUpWithGoogleDto) {
        User authenticatedUser = userService.signUpWithGoogle(signUpWithGoogleDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);
        LoginResponse loginResponse = LoginResponse.builder()
                .token(jwtToken)
                .expiresIn(jwtService.getExpirationTime())
                .build();

        return ResponseEntity.ok(loginResponse);
    }
}
