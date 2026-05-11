package com.example.demo.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.demo.Repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	private static final String VALID_USERNAME = "validuser";
	private static final String VALID_PASSWORD = "GoodPass123!";

	@Mock
	private UserRepository repo;

	private UserService subject;

	private BCryptPasswordEncoder encoder;

	@BeforeEach
	void setUp() {
		encoder = Mockito.spy(new BCryptPasswordEncoder());
		subject = new UserService(repo, encoder);
	}

	@Test
	void signUp_rejectsShortUsername() {
		Optional<UUID> result = subject.signUp("ab", VALID_PASSWORD);
		assertTrue(result.isEmpty());
		verify(repo, never()).insert(anyString(), anyString());
	}

	@Test
	void signUp_rejectsExistingUsername() {
		when(repo.userNameExists(VALID_USERNAME)).thenReturn(true);
		Optional<UUID> result = subject.signUp(VALID_USERNAME, VALID_PASSWORD);
		assertTrue(result.isEmpty());
		verify(repo, never()).insert(anyString(), anyString());
	}

	@Test
	void signUp_rejectsPasswordTooShort() {
		Optional<UUID> result = subject.signUp(VALID_USERNAME, "Short1!");
		assertTrue(result.isEmpty());
		verify(repo, never()).insert(anyString(), anyString());
	}

	@Test
	void signUp_rejectsPasswordMissingComplexity() {
		Optional<UUID> lowerOnly = subject.signUp(VALID_USERNAME, "onlylowercase1!");
		Optional<UUID> upperOnly = subject.signUp(VALID_USERNAME, "ONLYUPPERCASE1!");
		Optional<UUID> noDigit = subject.signUp(VALID_USERNAME, "NoDigitPass!");
		Optional<UUID> noSpecial = subject.signUp(VALID_USERNAME, "NoSpecial123");
		assertTrue(lowerOnly.isEmpty());
		assertTrue(upperOnly.isEmpty());
		assertTrue(noDigit.isEmpty());
		assertTrue(noSpecial.isEmpty());
		verify(repo, never()).insert(anyString(), anyString());
	}

	@Test
	void signUp_insertsEncodedPassword() {
		UUID insertedId = UUID.randomUUID();
		when(repo.userNameExists(VALID_USERNAME)).thenReturn(false);
		when(repo.insert(anyString(), anyString())).thenReturn(insertedId);

		Optional<UUID> result = subject.signUp(VALID_USERNAME, VALID_PASSWORD);

		assertEquals(Optional.of(insertedId), result);
		verify(encoder).encode(VALID_PASSWORD);
		verify(repo).insert(Mockito.eq(VALID_USERNAME), anyString());
	}

	@Test
	void signUp_acceptsPasswordExactlyAtMinLength() {
		when(repo.userNameExists("u10")).thenReturn(false);
		// 10 chars: meets length + lower, upper, digit, special
		String passwordTen = "Aa1!aaaaaa";
		assertEquals(10, passwordTen.length());
		UUID id = UUID.randomUUID();
		when(repo.insert(anyString(), anyString())).thenReturn(id);

		Optional<UUID> result = subject.signUp("u10", passwordTen);

		assertEquals(Optional.of(id), result);
	}

	@Test
	void signUp_rejectsPasswordOneBelowMinLength() {
		// 9 chars total — below minPasswordLength 10
		Optional<UUID> result = subject.signUp(VALID_USERNAME, "Aa1!aaaaa");
		assertTrue(result.isEmpty());
		verify(repo, never()).insert(anyString(), anyString());
	}

	@Test
	void signUp_acceptsUnicodeUsernameWhenLengthOk() {
		String unicodeUser = "ñ名工";
		when(repo.userNameExists(unicodeUser)).thenReturn(false);
		UUID id = UUID.randomUUID();
		when(repo.insert(Mockito.eq(unicodeUser), anyString())).thenReturn(id);

		Optional<UUID> result = subject.signUp(unicodeUser, VALID_PASSWORD);

		assertEquals(Optional.of(id), result);
	}

	@Test
	void signUp_rejectsWhitespaceOnlyPassword() {
		Optional<UUID> result = subject.signUp(VALID_USERNAME, "          ");
		assertTrue(result.isEmpty());
		verify(repo, never()).insert(anyString(), anyString());
	}
}
