package com.example.demo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OAuth2LoginController.class)
class DemoApplicationTests {

	@Autowired
	MockMvc mvc;

	@MockBean
	ClientRegistrationRepository clientRegistrationRepository;

	@TestConfiguration
	static class AuthorizedClient {
		@Bean
		public OAuth2AuthorizedClientRepository authorizedClientRepository() {
			return new HttpSessionOAuth2AuthorizedClientRepository();
		}
	}

	@Test
	public void rootWhenAuthenticatedReturnsUserAndClient() throws Exception {
		this.mvc.perform(get("/").with(oauth2Login()))
			.andExpect(model().attribute("userName", "test-subject"))
			.andExpect(model().attribute("clientName", "test"))
			.andExpect(model().attribute("userAttributes", Collections.singletonMap("sub", "test-subject")));
	}

}
