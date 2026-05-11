package com.example.demo.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.Models.MyUser;
import com.example.demo.Repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class ContactsServiceTest {

	@Mock
	private UserRepository userRepository;

	private ContactsService subject;

	@BeforeEach
	void setUp() {
		subject = new ContactsService(userRepository);
	}

	@AfterEach
	void clearSecurity() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void getMyContacts_throwsWhenNotLoggedIn() {
		assertThrows(InsufficientAuthenticationException.class, subject::getMyContacts);
	}

	@Test
	void getMyContacts_delegatesWithCurrentUsername() {
		var auth = UsernamePasswordAuthenticationToken.authenticated(
				"alice",
				"n/a",
				java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(auth);
		List<MyUser> contacts = Collections.emptyList();
		when(userRepository.getAllExcept("alice")).thenReturn(contacts);

		assertEquals(contacts, subject.getMyContacts());
		verify(userRepository).getAllExcept("alice");
	}
}
