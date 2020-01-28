package com.example.resource;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootApplication(proxyBeanMethods = false)
public class ResourceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResourceApplication.class, args);
	}

}

@RestController
class HomeController {

	@GetMapping("/")
	public String index(@AuthenticationPrincipal OAuth2AuthenticatedPrincipal user) {
		return "Hello " + (user != null ? user.getName() : "Nobody");
	}
}

@Configuration(proxyBeanMethods = false)
@Profile("azure")
class UserInfoTokenInspectorConfiguration {

	@Value("${azure.user-info-uri}")
	String userInfoUri;

	@Bean
	public OpaqueTokenIntrospector userInfoTokenInspector() {
		return new UserInfoOpaqueTokenIntrospector(userInfoUri);
	}
}

class UserInfoOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

	private static final ParameterizedTypeReference<Map<String, Object>> PARAMETERIZED_RESPONSE_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};

	private RestTemplate rest = new RestTemplate();

	private String userInfoUri;

	public UserInfoOpaqueTokenIntrospector(String userInfoUri) {
		this.userInfoUri = userInfoUri;
	}

	@Override
	public OAuth2AuthenticatedPrincipal introspect(String token) {

		HttpMethod httpMethod = HttpMethod.GET;
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
		URI uri = UriComponentsBuilder.fromUriString(userInfoUri).build().toUri();

		headers.setBearerAuth(token);
		RequestEntity<?> request = new RequestEntity<>(headers, httpMethod, uri);

		ResponseEntity<Map<String, Object>> response = rest.exchange(request, PARAMETERIZED_RESPONSE_TYPE);
		if (response.getStatusCode().is2xxSuccessful()) {
			Set<GrantedAuthority> authorities = new LinkedHashSet<>();
			Map<String, Object> attrs = response.getBody();
			String subject = "sub";
			if (attrs.containsKey("email")) {
				subject = "email";
			}
			authorities.add(new OAuth2UserAuthority(attrs));
			return new DefaultOAuth2User(authorities, attrs, subject);
		} else {
			OAuth2Error oauth2Error = new OAuth2Error(OAuth2ErrorCodes.ACCESS_DENIED, "UserInfoEndpoint failed", null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
		}

	}

}