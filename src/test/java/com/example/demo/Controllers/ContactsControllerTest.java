package com.example.demo.Controllers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.Models.MyUser;
import com.example.demo.SecurityConfig;
import com.example.demo.Services.ContactsService;

@WebMvcTest(ContactsController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class ContactsControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private ContactsService contactsService;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void myContacts_escapesHtmlInUsernames() throws Exception {
		MyUser nasty = new MyUser();
		nasty.id = UUID.randomUUID();
		nasty.username = "<alice>&\"'";
		when(contactsService.getMyContacts()).thenReturn(List.of(nasty));

		String html = mockMvc.perform(get("/contacts")
						.with(user("bob").roles("USER")))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString(StandardCharsets.UTF_8);

		assertTrue(html.contains("&lt;alice&gt;&amp;&quot;&#39;"));
		assertTrue(html.contains("href=\"/chat?contact_name="));
		assertFalse(html.contains("<script"), "response should not contain raw SCRIPT open tags from username");
	}
}
