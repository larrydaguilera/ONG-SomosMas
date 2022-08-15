package com.alkemy.ong.context;


	import org.springframework.boot.test.context.TestConfiguration;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Primary;
	import org.springframework.security.core.userdetails.User;
	import org.springframework.security.core.userdetails.UserDetails;
	import org.springframework.security.core.userdetails.UserDetailsService;
	import org.springframework.security.provisioning.InMemoryUserDetailsManager;

	import java.util.List;
	@TestConfiguration
	public class InMemoryConfig {

	   @Bean
	   @Primary
	   public UserDetailsService createUsers() {

	      UserDetails user = builderUser("user@gtest.com", "USER");
	      UserDetails admin = builderUser("admin@test.com", "ADMIN");

	      return new InMemoryUserDetailsManager(List.of(user, admin));
	   }

	   private UserDetails builderUser(String email, String role) {
	      return User.withUsername(email).password("password").roles(role).build();
	   }
	}