package com.example.demo.Controllers;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Services.UserService;

/**
 * Standalone MockMvc — /public/** has no security filters; covers mapping and HTML shaping.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

	@Mock
	private UserService userService;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		UserController controller = new UserController(userService);
		mockMvc = standaloneSetup(controller).build();
	}

	@Test
	void signup_returns201AndUuidOnSuccess() throws Exception {
		UUID id = UUID.randomUUID();
		when(userService.signUp("alice", "GoodPass123!")).thenReturn(Optional.of(id));

		mockMvc.perform(post("/public/signup")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "alice")
				.param("password_unhashed", "GoodPass123!"))
				.andExpect(status().isCreated())
				.andExpect(content().string(id.toString()));
	}

	@Test
	void signup_returns400WhenServiceRejects() throws Exception {
		when(userService.signUp("alice", "bad")).thenReturn(Optional.empty());

		mockMvc.perform(post("/public/signup")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "alice")
				.param("password_unhashed", "bad"))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Could not sign up"));
	}

	@Test
	void validateSignupInfo_returnsEmptyParagraphWhenValid() throws Exception {
		when(userService.getInvalidInputReason("u", "p")).thenReturn(Optional.empty());

		mockMvc.perform(post("/public/html/validateSignupInfo")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "u")
				.param("password_unhashed", "p"))
				.andExpect(status().isOk())
				.andExpect(content().string("<p></p>"));
	}

	@Test
	void validateSignupInfo_embedsReasonHtmlUnescaped() throws Exception {
		when(userService.getInvalidInputReason(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
				.thenReturn(Optional.of("<script>alert(1)</script>"));

		String body = mockMvc.perform(post("/public/html/validateSignupInfo")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "u")
				.param("password_unhashed", "p"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8);

		assertTrue(body.contains("<script>alert(1)</script>"));
	}

	@Test
	void validateSignupInfo_handlesSqlLikePayloadsInParamsWithoutCrashing() throws Exception {
		when(userService.getInvalidInputReason("a' OR 1=1--", "p")).thenReturn(Optional.of("bad"));

		mockMvc.perform(post("/public/html/validateSignupInfo")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "a' OR 1=1--")
				.param("password_unhashed", "p"))
				.andExpect(status().isOk());

		verify(userService).getInvalidInputReason("a' OR 1=1--", "p");
	}

	@Test
	void signupHtml_returnsValidationFragmentWhenSignUpFails() throws Exception {
		when(userService.signUp("u", "p")).thenReturn(Optional.empty());
		when(userService.getInvalidInputReason("u", "p")).thenReturn(Optional.of("nope"));

		mockMvc.perform(post("/public/html/signup")
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("username", "u")
				.param("password_unhashed", "p"))
				.andExpect(status().isOk())
				.andExpect(content().string("<p>nope</p>"));
	}
}
