package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

/**
 * @author Joe Grandja
 * @author Rob Winch
 */
@Controller
class OAuth2LoginController {

	@Value("${resource.url}")
	String url;
	private RestTemplateBuilder builder;

	OAuth2LoginController(RestTemplateBuilder builder) {
		this.builder = builder;
	}

	@GetMapping("/")
	public String index(Model model, @RegisteredOAuth2AuthorizedClient OAuth2AuthorizedClient authorizedClient,
			@AuthenticationPrincipal OAuth2User oauth2User) {
		model.addAttribute("accessToken", authorizedClient.getAccessToken());
		model.addAttribute("userName", oauth2User.getName());
		model.addAttribute("clientName", authorizedClient.getClientRegistration().getClientName());
		model.addAttribute("userAttributes", oauth2User.getAttributes());
		try {
			RestTemplate rest = builder.additionalRequestCustomizers(request -> {
				request.getHeaders().add("Authorization",
						"Bearer " + authorizedClient.getAccessToken().getTokenValue());
			}).rootUri(url).build();
			model.addAttribute("message", rest.getForObject("/", String.class));
		} catch (Exception e) {
			model.addAttribute("message", "ERROR: " + e.getMessage());
		}
		return "index";
	}
}
