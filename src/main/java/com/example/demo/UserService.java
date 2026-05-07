package com.example.demo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

class SignupResult {
}


@Service
public class UserService {
    @Autowired
    private UserRepository repo;

    private PasswordEncoder encoder = new BCryptPasswordEncoder();

    final int minPasswordLength = 10;

    /* 
    ===================
    Sign up
    ===================
    */
    public Optional<UUID> signUp(String username, String password_unhashed) {
        if (!getInvalidInputReason(username, password_unhashed).isEmpty()) {
            return Optional.empty();
        }
        String password_hashed = encoder.encode(password_unhashed);

        return Optional.of(repo.insert(username, password_hashed));
    }

    // We want to return the reason for an invalid signup data, or empty if it's good!
    // This is cause of HTMX
    public Optional<String> getInvalidInputReason(String username, String password) {
        if (repo.userNameExists(username)) {
            return Optional.of("Username already exists!");
        }

        return getPasswordInvalidReason(password);
    }

    // We want to return the reason for an invalid password, or empty if it's good!
    // This is cause of HTMX
    private Optional<String> getPasswordInvalidReason(String password) {
        if (password.length() < minPasswordLength) {
            return Optional.of("Password must be at least 12 characters");
        }

        boolean containsLowercase = false;
        boolean containsUppercase = false;
        boolean containsNumber = false;
        boolean containsSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isLowerCase(c)) {
                containsLowercase = true;
            }
            else if (Character.isUpperCase(c)) {
                containsUppercase = true;
            }
            else if (Character.isDigit(c)) {
                containsNumber = true;
            }
            else {
                containsSpecial = true;
            }
        }
        if (!containsLowercase) {
            return Optional.of("Password must contain at least one lowercase letter");
        }
        else if (!containsUppercase) {
            return Optional.of("Password must contain at least one uppercase letter");
        }
        if (!containsNumber) {
            return Optional.of("Password must contain at least one number!");
        }
        if (!containsSpecial) {
            return Optional.of("Password must contain at least one Special Character (EG: !, or $");
        }
        return Optional.empty();
    }

    /* 
    ===================
    Log In
    ===================
    */

    public Optional<UUID> login(String username, String password_unhashed) {
        MyUser u = repo.getByUsername(username);
        boolean correctPass = encoder.matches(password_unhashed, u.password_hashed);
        if (correctPass) return Optional.of(u.id);
        else return Optional.empty();
    }
}
