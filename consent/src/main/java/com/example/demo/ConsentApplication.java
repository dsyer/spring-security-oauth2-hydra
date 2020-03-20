package com.example.demo;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class ConsentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsentApplication.class, args);
	}

}

@EnableWebSecurity
class SecurityConfig extends WebSecurityConfigurerAdapter {

	// @formatter:off
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
				.authorizeRequests(authorize -> authorize
					.antMatchers("/css/**", "/login", "/error").permitAll()
					.antMatchers("/").hasRole("USER")
				)
				.formLogin(formLogin -> formLogin
					.successHandler(successHandler())
					.failureHandler(failureHandler())
					.loginPage("/login")
				);
	}
	// @formatter:on

	private AuthenticationFailureHandler failureHandler() {
		return (req, res, ex) -> req.getRequestDispatcher("/login?error").forward(req, res);
	}

	private AuthenticationSuccessHandler successHandler() {
		return (req, res, auth) -> req.getRequestDispatcher("/").forward(req, res);
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails userDetails = User.withDefaultPasswordEncoder().username("user").password("password").roles("USER")
				.build();
		return new InMemoryUserDetailsManager(userDetails);
	}
}

@Controller
class LoginController {

	private static ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<Map<String, Object>>() {
	};

	private String rootUrl;
	private RestTemplate rest;

	LoginController(RestTemplateBuilder builder, @Value("${admin.url}") String url) {
		this.rest = builder.build();
		rootUrl = url + "/oauth2/auth/requests";
	}

	@GetMapping("/login")
	public String login(Model model, @RequestParam(name = "login_challenge", required = false) String challenge) {
		model.addAttribute("login_challenge", challenge);
		if (!StringUtils.hasText(challenge)) {
			unauthenticated(model, challenge);
		}
		Map<String, Object> response = get("login", challenge);
		Boolean skip = (Boolean) response.get("skip");
		if (skip != null && skip) {
			return authenticated(model, new UsernamePasswordAuthenticationToken(response.get("subject"), "",
					AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER")), challenge);
		}
		return "login";
	}

	// Spring Security sends us back to /login if there is an authentication error
	@PostMapping("/login")
	public String reject(Model model, @RequestParam(name = "login_challenge", required = false) String challenge) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "Unauthorized");
		body.put("error_description", "Could not process login request");
		Map<String, Object> response = reject("login", challenge, body);
		return "redirect:" + response.get("redirect_to");
	}

	// Spring Security forwards to "/" by default on successful authentication
	@PostMapping("/")
	public String home(Model model, @AuthenticationPrincipal Authentication principal,
			@RequestParam(name = "login_challenge", required = false) String challenge) {
		if (!StringUtils.hasText(challenge)) {
			unauthenticated(model, challenge);
		}
		model.addAttribute("login_challenge", challenge);
		if (principal == null) {
			principal = SecurityContextHolder.getContext().getAuthentication();
		}
		if (principal == null || !principal.isAuthenticated()) {
			return reject(model, challenge);
		}
		return authenticated(model, principal, challenge);
	}

	@GetMapping("/consent")
	public String consent(Model model, @RequestParam(name = "consent_challenge", required = false) String challenge) {
		if (!StringUtils.hasText(challenge)) {
			unauthenticated(model, challenge);
		}
		Map<String, Object> response = get("consent", challenge);
		Boolean skip = (Boolean) response.get("skip");
		if (skip != null && skip) {
			return consented(model, response, challenge);
		}
		// TODO: display UI for consent and accept POST for response
		return consented(model, response, challenge);
	}

	private String consented(Model model, Map<String, Object> grant, String challenge) {
		Map<String, Object> body = new HashMap<>();
		body.put("grant_scope", grant.get("requested_scope"));
		body.put("grant_access_token_audience", grant.get("requested_access_token_audience"));
		Map<String, Object> response = accept("consent", challenge, body);
		return "redirect:" + response.get("redirect_to");
	}

	private String authenticated(Model model, @AuthenticationPrincipal Authentication principal,
			@RequestParam("login_challenge") String challenge) {
		Map<String, Object> body = new HashMap<>();
		body.put("subject", principal.getName());
		body.put("remember", false);
		Map<String, Object> response = accept("login", challenge, body);
		return "redirect:" + response.get("redirect_to");
	}

	private String unauthenticated(Model model, String challenge) {
		Map<String, Object> body = new HashMap<>();
		body.put("error", "Denied");
		body.put("error_description", "Could not process login request");
		Map<String, Object> response = reject("login", challenge, body);
		return "redirect:" + response.get("redirect_to");
	}

	private Map<String, Object> get(String path, String key, String value) {
		String uri = rootUrl + "/{path}?{key}={value}";
		return rest.exchange(uri, HttpMethod.GET, null, MAP_TYPE, path, key, value).getBody();
	}

	private Map<String, Object> get(String path, String value) {
		String key = path + "_challenge";
		return get(path, key, value);
	}

	private Map<String, Object> put(String path, String key, String value, Map<String, Object> body) {
		String uri = rootUrl + "/{path}?{key}={value}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		return rest.exchange(uri, HttpMethod.PUT, new HttpEntity<>(body, headers), MAP_TYPE, path, key, value)
				.getBody();
	}

	private Map<String, Object> reject(String path, String value, Map<String, Object> body) {
		String key = path + "_challenge";
		path = path + "/reject";
		return put(path, key, value, body);
	}

	private Map<String, Object> accept(String path, String value, Map<String, Object> body) {
		String key = path + "_challenge";
		path = path + "/accept";
		return put(path, key, value, body);
	}

}
