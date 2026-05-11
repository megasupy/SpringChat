package com.example.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthHelperTest {

	@AfterEach
	void clear() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void isLoggedIn_falseWhenNoAuthentication() {
		assertFalse(AuthHelper.isLoggedIn());
	}

	@Test
	void isLoggedIn_falseWhenExplicitlyUnauthenticated() {
		var auth = UsernamePasswordAuthenticationToken.unauthenticated("x", "y");
		SecurityContextHolder.getContext().setAuthentication(auth);
		assertFalse(AuthHelper.isLoggedIn());
	}

	@Test
	void isLoggedIn_trueWhenAuthenticatedToken() {
		var auth = UsernamePasswordAuthenticationToken.authenticated(
				"alice",
				"n/a",
				java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(auth);
		assertTrue(AuthHelper.isLoggedIn());
	}

	@Test
	void getAuthThrowsWhenNullAuthentication() {
		assertThrows(InsufficientAuthenticationException.class, AuthHelper::getAuth);
	}

	@Test
	void getAuthThrowsWhenUnauthenticated() {
		var auth = UsernamePasswordAuthenticationToken.unauthenticated("x", "y");
		SecurityContextHolder.getContext().setAuthentication(auth);
		assertThrows(InsufficientAuthenticationException.class, AuthHelper::getAuth);
	}

	@Test
	void getAuthReturnsAuthenticationWhenAuthenticated() {
		var auth = UsernamePasswordAuthenticationToken.authenticated(
				"alice",
				"n/a",
				java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(auth);
		assertSame(auth, AuthHelper.getAuth());
	}

	@Test
	void getUserNameReturnsPrincipalName() {
		var auth = UsernamePasswordAuthenticationToken.authenticated(
				"alice",
				"n/a",
				java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(auth);
		assertEquals("alice", AuthHelper.getUserName());
	}
}
