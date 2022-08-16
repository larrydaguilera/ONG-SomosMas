package com.alkemy.ong.controller;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.alkemy.ong.context.ContextTest;
import com.alkemy.ong.models.entity.RoleEntity;
import com.alkemy.ong.models.mapper.CategoryMapper;
import com.alkemy.ong.models.mapper.UserMapper;
import com.alkemy.ong.models.request.AuthRequest;
import com.alkemy.ong.models.request.CategoryRequest;
import com.alkemy.ong.models.request.UserRequest;
import com.alkemy.ong.repository.CategoryRepository;
import com.alkemy.ong.repository.RoleRepository;
import com.alkemy.ong.repository.UserRepository;
import com.alkemy.ong.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@RunWith(SpringRunner.class)

public class AuthControllerTest extends ContextTest{
		
	private static final String AUTH_ME_URL = "/auth/me";

	@Test
	public void should_return_user_created() throws Exception {	
		UserRequest userRequest = UserRequest.builder()
				.firstName("user")
				.lastName("user")
				.email("usertest@test.com")
				.password("12345678")
				.build();

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(REGISTER_URL)
						.content(objectMapper.writeValueAsString(userRequest))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated());
		
	}

	@Test
	public void should_return_bad_request() throws Exception {
		UserRequest badUserRequest = UserRequest.builder()
				.firstName("test")
				.lastName("test")
				.email(null)
				.password("12345678")
				.build();

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(REGISTER_URL)
						.content(objectMapper.writeValueAsString(badUserRequest))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isBadRequest());	
	}

	@Test
	public void should_return_conflict_already_exists() throws Exception, IOException {
		postUser();
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(REGISTER_URL)
						.content(objectMapper.writeValueAsString(userRequest))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isConflict());	
	}


	@Test
	public void should_return_200ok_at_login_with_registered_user() throws Exception, IOException {
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(AUTH_LOGIN_URL)
						.content(objectMapper.writeValueAsString(
								AuthRequest.builder()
								.email("user@test.com")
								.password("12345678")
								.build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.email", equalTo("user@test.com")))
				.andExpect(jsonPath("$.token", notNullValue()))
				.andExpect(MockMvcResultMatchers.status().isOk());
	}

	@Test
	public void should_return_ok_false_when_password_is_invalid() throws Exception, IOException {		
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(AUTH_LOGIN_URL)
						.content(objectMapper.writeValueAsString(
							AuthRequest.builder()
							.email("admin@test.com")
							.password("invalid pass")
							.build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.ok", equalTo(false)));	
	}

	@Test
	public void should_return_user_details_after_login() throws Exception, IOException {
		postUser();
		String content = this.mockMvc
				.perform(post(AUTH_LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								AuthRequest.builder()
								.email("user@test.com")
								.password("12345678")
								.build())))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);		
		String token = JsonPath.read(content, "$.token");
		
		this.mockMvc
				.perform(MockMvcRequestBuilders.get(AUTH_ME_URL)
						.header(HttpHeaders.AUTHORIZATION, BEARER + token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.firstName", equalTo(userRequest.getFirstName())))
				.andExpect(jsonPath("$.lastName", equalTo(userRequest.getLastName())))
				.andExpect(jsonPath("$.email", equalTo(userRequest.getEmail())))
				.andExpect(status().isOk());
	}
	  
}
