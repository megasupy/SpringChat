package com.example.demo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {
    @Autowired
    UserService service;

    @PostMapping("/auth/signup")
    ResponseEntity<String> signUp(@RequestParam String username, @RequestParam String password_unhashed) {
        Optional<UUID> userID = service.signUp(username, password_unhashed);
        if (userID.isEmpty()) {
            return ResponseEntity.badRequest().body("Could not sign up");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userID.get().toString());
    }


    @PostMapping("/html/auth/validateSignupInfo") 
    String getSignupValidationHTML(@RequestParam String username, @RequestParam String password_unhashed) {
        Optional<String> reason = service.getInvalidInputReason(username, password_unhashed);
        if (reason.isEmpty()) {
            return "<p></p>"; 
        }
        return "<p>" + reason.get() + "</p>";
    }

    @PostMapping("/html/auth/signup") 
    String signUpHTML(@RequestParam String username, @RequestParam String password_unhashed) {
        Optional<UUID> userID = service.signUp(username, password_unhashed);
        if (userID.isEmpty()) {
            return getSignupValidationHTML(username, password_unhashed);
        }

        return "<p> Created user with ID " + userID.get().toString() + "</p>";
    }

    @PostMapping("/html/auth/login")
    String loginHTML(@RequestParam String username, @RequestParam String password_unhashed) {
        Optional<UUID> result = service.login(username, password_unhashed);
        if (result.isEmpty()) {
            return "<p>Wrong Password!</p>";
        }
        return "<p> successfully entered info for ID: " + result.get().toString() + "</p>";
    }
}
