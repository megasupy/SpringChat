package com.example.demo.Controllers;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.SecurityConfig;

@WebMvcTest(PageController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class PageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private UserDetailsService userDetailsService;

	@Test
	void index_returnsIndexView() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
	}

	@Test
	void login_returnsLoginView() throws Exception {
		mockMvc.perform(get("/login")).andExpect(status().isOk()).andExpect(view().name("login"));
	}

	@Test
	void signup_returnsSignupView() throws Exception {
		mockMvc.perform(get("/signup")).andExpect(status().isOk()).andExpect(view().name("signup"));
	}

	@Test
	void chat_reflectsRawContactNameIntoModel_whenAuthenticated() throws Exception {
		String payload = "<img src=x onerror=alert(1)>";
		mockMvc.perform(get("/chat")
						.param("contact_name", payload)
						.with(user("alice").roles("USER")))
				.andExpect(status().isOk())
				.andExpect(view().name("chat"))
				.andExpect(model().attribute("contact_name", payload))
				.andExpect(model().attribute("has_contact", true));
	}

	@Test
	void chat_withoutContact_setsHasContactFalse() throws Exception {
		mockMvc.perform(get("/chat").with(user("alice").roles("USER")))
				.andExpect(status().isOk())
				.andExpect(view().name("chat"))
				.andExpect(model().attribute("contact_name", ""))
				.andExpect(model().attribute("has_contact", false));
	}
}
