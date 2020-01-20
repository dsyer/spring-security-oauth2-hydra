package com.example.resource;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ResourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceApplication.class, args);
	}

}

@RestController
class HomeController {

	@GetMapping("/")
	public String index(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal user) {
		return "Hello " + (user !=null ? user.getName() : "Nobody");
	}
}
