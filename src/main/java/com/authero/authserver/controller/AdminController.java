package com.authero.authserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.authero.authserver.dto.SignupDto;
import com.authero.authserver.models.User;
import com.authero.authserver.service.UserService;

@RequestMapping("/admins")
@RestController
public class AdminController {

    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<User> createAdministrator(@RequestBody SignupDto signupDto) {
        User createdAdmin = userService.createAdministrator(signupDto);

        return ResponseEntity.ok(createdAdmin);
    }
}