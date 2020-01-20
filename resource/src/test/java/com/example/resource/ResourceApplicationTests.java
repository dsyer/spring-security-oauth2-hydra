package com.example.resource;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.opaqueToken;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HomeController.class)
class DemoApplicationTests {

	@Autowired
	MockMvc mvc;

	@Test
	public void messageCanBeReadWithScopeMessageReadAuthority() throws Exception {
		this.mvc.perform(get("/").with(opaqueToken().scopes("openid")))
				.andExpect(content().string(is("Hello user")));
	}

}
