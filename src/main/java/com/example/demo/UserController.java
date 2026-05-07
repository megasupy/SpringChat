package com.example.demo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {
    @Autowired
    UserService service;

    private final AuthenticationManager authenticationManager;

    public UserController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/public/signup")
    ResponseEntity<String> signUp(@RequestParam String username, @RequestParam String password_unhashed) {
        Optional<UUID> userID = service.signUp(username, password_unhashed);
        if (userID.isEmpty()) {
            return ResponseEntity.badRequest().body("Could not sign up");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userID.get().toString());
    }


    @PostMapping("/public/html/validateSignupInfo") 
    String getSignupValidationHTML(@RequestParam String username, @RequestParam String password_unhashed) {
        Optional<String> reason = service.getInvalidInputReason(username, password_unhashed);
        if (reason.isEmpty()) {
            return "<p></p>"; 
        }
        return "<p>" + reason.get() + "</p>";
    }

    @PostMapping("/public/html/signup") 
    String signUpHTML(@RequestParam String username, @RequestParam String password_unhashed) {
        Optional<UUID> userID = service.signUp(username, password_unhashed);
        if (userID.isEmpty()) {
            return getSignupValidationHTML(username, password_unhashed);
        }

        return "<p> Created user with ID " + userID.get().toString() + "</p>";
    }

    @PostMapping("/public/login")
    String loginHTML(@RequestParam String username, @RequestParam String password) {
        Authentication authenticationRequest =
			UsernamePasswordAuthenticationToken.unauthenticated(username, password);
		Authentication authenticationResponse =
			this.authenticationManager.authenticate(authenticationRequest);

        return "<p>Success!</p>";

        /*
        Optional<UUID> result = service.login(username, password);
        if (result.isEmpty()) {
            return "<p>Wrong Password!</p>";
        }
        return "<p> successfully entered info for ID: " + result.get().toString() + "</p>";
        */
    }
}
