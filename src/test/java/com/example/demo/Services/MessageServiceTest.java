package com.example.demo.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.Models.Message;
import com.example.demo.Models.MyUser;
import com.example.demo.Repositories.MessageRepository;
import com.example.demo.Repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@Mock
	private MessageRepository messageRepository;

	@Mock
	private UserRepository userRepository;

	private MessageService subject;

	@BeforeEach
	void setUp() {
		subject = new MessageService(messageRepository, userRepository);
	}

	@AfterEach
	void clearSecurity() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void sendMessage_byNames_throwsWhenNotLoggedIn() {
		assertThrows(InsufficientAuthenticationException.class, () -> subject.sendMessage("bob", "hello"));
		verifyNoInteractions(messageRepository);
	}

	@Test
	void sendMessage_byNames_throwsWhenMessageBlank() {
		setAuthenticatedUser("alice");

		assertThrows(IllegalArgumentException.class, () -> subject.sendMessage("bob", ""));
		verifyNoInteractions(messageRepository);
	}

	@Test
	void sendMessage_byNames_insertsWhenValid() {
		setAuthenticatedUser("alice");
		UUID aliceId = UUID.randomUUID();
		UUID bobId = UUID.randomUUID();
		MyUser alice = new MyUser();
		alice.id = aliceId;
		alice.username = "alice";
		MyUser bob = new MyUser();
		bob.id = bobId;
		bob.username = "bob";
		when(userRepository.getByUsername("alice")).thenReturn(alice);
		when(userRepository.getByUsername("bob")).thenReturn(bob);

		subject.sendMessage("bob", "Hello there");

		ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
		verify(messageRepository).insert(captor.capture());
		Message saved = captor.getValue();
		assertEquals(aliceId, saved.sender_id);
		assertEquals(bobId, saved.recipient_id);
		assertEquals("Hello there", saved.message);
	}

	@Test
	void getConversation_throwsWhenNotLoggedIn() {
		assertThrows(InsufficientAuthenticationException.class, () -> subject.getConversation("bob", 10, 0));
	}

	@Test
	void getConversation_throwsWhenLimitExceedsMax() {
		setAuthenticatedUser("alice");
		assertThrows(IllegalArgumentException.class, () -> subject.getConversation("bob", 101, 0));
		verifyNoInteractions(messageRepository);
	}

	@Test
	void getConversation_limitAtMaxPasses() {
		setAuthenticatedUser("alice");
		UUID aliceId = UUID.randomUUID();
		UUID bobId = UUID.randomUUID();
		MyUser alice = new MyUser();
		alice.id = aliceId;
		alice.username = "alice";
		MyUser bob = new MyUser();
		bob.id = bobId;
		bob.username = "bob";
		when(userRepository.getByUsername("alice")).thenReturn(alice);
		when(userRepository.getByUsername("bob")).thenReturn(bob);
		when(messageRepository.getConversation(eq(aliceId), eq(bobId), eq(100), eq(0)))
				.thenReturn(Collections.emptyList());

		var conv = subject.getConversation("bob", 100, 0);
		assertEquals(aliceId, conv.currentUser.id);
		assertEquals(bobId, conv.contact.id);
		assertEquals(Collections.emptyList(), conv.conversation);
	}

	@Test
	void getConversation_passesNegativeOffsetThrough() {
		setAuthenticatedUser("alice");
		UUID aliceId = UUID.randomUUID();
		UUID bobId = UUID.randomUUID();
		MyUser alice = new MyUser();
		alice.id = aliceId;
		alice.username = "alice";
		MyUser bob = new MyUser();
		bob.id = bobId;
		bob.username = "bob";
		when(userRepository.getByUsername("alice")).thenReturn(alice);
		when(userRepository.getByUsername("bob")).thenReturn(bob);
		when(messageRepository.getConversation(eq(aliceId), eq(bobId), eq(10), eq(-1)))
				.thenReturn(Collections.emptyList());

		subject.getConversation("bob", 10, -1);
		verify(messageRepository).getConversation(aliceId, bobId, 10, -1);
	}

	@Test
	void sendMessage_entity_insertsWhenSenderMatchesAuthenticatedUser() {
		setAuthenticatedUser("alice");
		UUID aliceId = UUID.randomUUID();
		MyUser senderRow = new MyUser();
		senderRow.id = aliceId;
		senderRow.username = "alice";
		Message msg = new Message();
		msg.sender_id = aliceId;
		msg.recipient_id = UUID.randomUUID();
		msg.message = "hi";
		when(userRepository.get(aliceId)).thenReturn(senderRow);

		subject.sendMessage(msg);

		verify(messageRepository).insert(msg);
	}

	@Test
	void sendMessage_entity_rejectsForgedSender() {
		setAuthenticatedUser("alice");
		UUID victimId = UUID.randomUUID();
		MyUser victim = new MyUser();
		victim.id = victimId;
		victim.username = "victim";
		Message msg = new Message();
		msg.sender_id = victimId;
		when(userRepository.get(victimId)).thenReturn(victim);

		assertThrows(InsufficientAuthenticationException.class, () -> subject.sendMessage(msg));
		verifyNoInteractions(messageRepository);
	}

	private static void setAuthenticatedUser(String username) {
		var auth = UsernamePasswordAuthenticationToken.authenticated(
				username,
				"n/a",
				java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContextHolder.getContext().setAuthentication(auth);
	}
}
