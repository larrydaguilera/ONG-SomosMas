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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.alkemy.ong.models.entity.CategoryEntity;
import com.alkemy.ong.models.entity.RoleEntity;
import com.alkemy.ong.models.entity.UserEntity;
import com.alkemy.ong.models.mapper.CategoryMapper;
import com.alkemy.ong.models.mapper.UserMapper;
import com.alkemy.ong.models.request.AuthRequest;
import com.alkemy.ong.models.request.CategoryRequest;
import com.alkemy.ong.models.request.NewsRequest;
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
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

	protected static final String BEARER = "Bearer ";
	
	private static final String REGISTER_URL = "/auth/register";
	
	private static final String AUTH_LOGIN_URL = "/auth/login"
			;
	private static final String AUTH_ME_URL = "/auth/me";
	
	private static final String POST_NEWS_URL = "/news";

	private static final String GET_NEWS_BY_ID_URL = "/news/{id}";
	@Autowired
	public MockMvc mockMvc;
	@Autowired
	protected ObjectMapper objectMapper;
	@Autowired
	public UserMapper mapper;
	@Autowired
	public CategoryMapper categoryMapper;
	@Autowired
	public UserRepository userRepo;
	@Autowired
	public RoleRepository roleRepo;
	@Autowired
	public CategoryRepository categoryRepo;	
	@Autowired
	public AuthService authService;

	UserRequest userRequest;

	AuthRequest authRequest;
	
	NewsRequest newsRequest;
	
	CategoryRequest categoryRequest;
	
	Set<RoleEntity> roles;


	@Before()
	public void setup() {
		
		authRequest = AuthRequest.builder()
				.email("test@test.com")
				.password("12345678")
				.build();
		
		categoryRequest = CategoryRequest.builder()
				.name("test")
				.image("")
				.description("test category")
				.build();
		
		newsRequest = NewsRequest.builder()
				.name("test")
				.content("test")
				.image("test@test.com")
				.idCategory(null)
				.build();
		
	}

	@Test
	public void should_return_user_created() throws Exception {	
		userRequest = UserRequest.builder()
				.firstName("user")
				.lastName("user")
				.email("user@user.com")
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
		registerUser();
		
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(REGISTER_URL)
						.content(objectMapper.writeValueAsString(userRequest))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isConflict());
		
	}



	@Test
	public void should_return_200ok_at_login_with_registered_user() throws Exception, IOException {
		registerUser();
		
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(AUTH_LOGIN_URL)
						.content(objectMapper.writeValueAsString(
								AuthRequest.builder()
								.email(userRequest
								.getEmail())
								.password(userRequest.getPassword())
								.build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.email", equalTo(userRequest.getEmail())))
				.andExpect(jsonPath("$.token", notNullValue()))
				.andExpect(MockMvcResultMatchers.status().isOk());
		
	}

	@Test
	public void should_return_ok_false_when_password_is_invalid() throws Exception, IOException {		
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(AUTH_LOGIN_URL)
						.content(objectMapper.writeValueAsString(
							AuthRequest.builder()
							.email(authRequest
							.getEmail())
							.password("falsopass")
							.build()))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.ok", equalTo(false)));
		
	}

	@Test
	public void should_return_user_details_after_register_and_login() throws Exception, IOException {
		registerUser();

		String content = this.mockMvc
				.perform(post(AUTH_LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								AuthRequest.builder()
								.email("user@user.com")
								.password("12345678")
								.build())))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);		
		String token = JsonPath.read(content, "$.token");
		UserEntity user = mapper.toUserEntity(userRequest, roles);
		
		this.mockMvc
				.perform(MockMvcRequestBuilders.get(AUTH_ME_URL)
						.header(HttpHeaders.AUTHORIZATION, BEARER + token)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.firstName", equalTo(user.getFirstName())))
				.andExpect(jsonPath("$.lastName", equalTo(user.getLastName())))
				.andExpect(jsonPath("$.email", equalTo(user.getEmail())))
				.andExpect(status().isOk());

	}
	  
	@Test
	public void should_return_news() throws Exception, IOException {
		CategoryEntity  newCategory = 	categoryRepo.save(categoryMapper.Request2Entity(categoryRequest));		
		newsRequest.setIdCategory(newCategory.getId());

		String token = generateAdminToken();
		
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(POST_NEWS_URL)
						.header(HttpHeaders.AUTHORIZATION, BEARER + token )
						.content(objectMapper.writeValueAsString(newsRequest))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated());
		
	}
	
	private void registerUser() throws Exception, JsonProcessingException {
		userRequest = UserRequest.builder()
				.firstName("user")
				.lastName("user")
				.email("user@user.com")
				.password("12345678")
				.build();

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(REGISTER_URL)
						.content(objectMapper.writeValueAsString(userRequest))
						.contentType(MediaType.APPLICATION_JSON));
	}

	private String generateAdminToken() throws IOException, UnsupportedEncodingException, Exception, JsonProcessingException {
		userRequest = UserRequest.builder()
				.firstName("admin")
				.lastName("admin")
				.email("admin@test.com")
				.password("12345678")
				.build();
		authService.registerAdmin(userRequest);	
		
		String content = this.mockMvc
				.perform(post(AUTH_LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								AuthRequest.builder()
								.email("admin@test.com")
								.password("12345678")
								.build())))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);		
	
		return JsonPath.read(content, "$.token");
	}
	
}
