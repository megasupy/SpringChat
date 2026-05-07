package com.example.demo;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuthHelper {
    public static boolean isLoggedIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedin = auth != null
            && auth.isAuthenticated();
        return loggedin;
    }

    public static Authentication getAuth() throws InsufficientAuthenticationException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) 
            throw new InsufficientAuthenticationException("User not authenticated!");

        return auth;
    }

    public static String getUserName() throws InsufficientAuthenticationException {
        var auth = getAuth();
        return auth.getName();
    }
}
