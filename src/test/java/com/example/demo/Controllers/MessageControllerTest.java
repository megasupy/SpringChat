package com.example.demo.Controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Models.Message;
import com.example.demo.Models.MyUser;
import com.example.demo.SecurityConfig;
import com.example.demo.Services.MessageService;
import com.example.demo.Repositories.MessageRepository;
import com.example.demo.Repositories.UserRepository;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class MessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private MessageService messageService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void postMessage_withoutCsrf_returnsForbidden() throws Exception {
		when(messageService.isValidMessageText("x")).thenReturn(true);

		mockMvc.perform(post("/message")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("contact_name", "bob")
				.param("message_text", "x")
				.with(user("alice").roles("USER")))
				.andExpect(status().isForbidden());
	}

	@Test
	void postMessage_invalidText_returnsOkWithErrorBody() throws Exception {
		when(messageService.isValidMessageText("")).thenReturn(false);

		String body = mockMvc.perform(post("/message")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("contact_name", "bob")
				.param("message_text", "")
				.with(csrf())
				.with(user("alice").roles("USER")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8);

		assertTrue(body.contains("Error: Message is Empty"));
	}

	@Test
	void postMessage_validText_delegatesToService() throws Exception {
		when(messageService.isValidMessageText("hello")).thenReturn(true);

		mockMvc.perform(post("/message")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("contact_name", "bob")
				.param("message_text", "hello")
				.with(csrf())
				.with(user("alice").roles("USER")))
				.andExpect(status().isOk())
				.andExpect(content().string("Inserted Message"));

		verify(messageService).sendMessage("bob", "hello");
	}

	@Test
	void getMessage_badLimit_returns400() throws Exception {
		mockMvc.perform(get("/message")
						.param("contact_name", "bob")
						.param("limit", "-1")
						.with(user("alice").roles("USER")))
				.andExpect(status().isBadRequest());
		mockMvc.perform(get("/message")
						.param("contact_name", "bob")
						.param("limit", "101")
						.with(user("alice").roles("USER")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getMessage_escapesHtmlInConversation() throws Exception {
		UUID aliceId = UUID.randomUUID();
		UUID bobId = UUID.randomUUID();
		MyUser alice = new MyUser();
		alice.id = aliceId;
		alice.username = "alice";
		MyUser bob = new MyUser();
		bob.id = bobId;
		bob.username = "bob";

		MessageService.Conversation conv = new MessageService(
				org.mockito.Mockito.mock(MessageRepository.class),
				org.mockito.Mockito.mock(UserRepository.class)).new Conversation();
		Message msg = new Message();
		msg.sender_id = bobId;
		msg.recipient_id = aliceId;
		msg.message = "<script>alert(1)</script>";
		conv.conversation = List.of(msg);
		conv.currentUser = alice;
		conv.contact = bob;
		when(messageService.getConversation(eq("<evil>"), eq(10), eq(0))).thenReturn(conv);

		String html = mockMvc.perform(get("/message")
						.param("contact_name", "<evil>")
						.param("limit", "10")
						.param("offset", "0")
						.with(user("alice").roles("USER")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8);

		assertTrue(html.contains("&lt;script&gt;"));
		assertTrue(html.contains("&lt;evil&gt;"));
	}

	@Test
	void postMessageDeprecated_whenServiceRejectsSender_returnsErrorStatus() throws Exception {
		doThrow(new InsufficientAuthenticationException("no"))
				.when(messageService).sendMessage(any(Message.class));

		String json = """
				{"sender_id":"%s","recipient_id":"%s","message":"hi"}
				""".formatted(UUID.randomUUID(), UUID.randomUUID());

		mockMvc.perform(post("/message/deprecated")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json)
						.with(csrf())
						.with(user("alice").roles("USER")));

		verify(messageService).sendMessage(any(Message.class));
	}
}
