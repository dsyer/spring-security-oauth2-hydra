package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LoginController.class)
class DemoApplicationTests {

	@Autowired
	MockMvc mvc;

	@MockBean
	RestTemplateBuilder builder;

	@Test
	public void contextLoads() throws Exception {
	}

}
