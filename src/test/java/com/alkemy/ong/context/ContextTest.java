package com.alkemy.ong.context;

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
import org.springframework.test.context.ActiveProfiles;
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
import com.alkemy.ong.models.response.UserResponse;
import com.alkemy.ong.repository.CategoryRepository;
import com.alkemy.ong.repository.RoleRepository;
import com.alkemy.ong.repository.UserRepository;
import com.alkemy.ong.service.AuthService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class ContextTest {

	protected static final String BEARER = "Bearer ";
	
	protected static final String REGISTER_URL = "/auth/register";
	
	protected static final String AUTH_LOGIN_URL = "/auth/login";
			
	protected UserRequest userRequest;

	AuthRequest authRequest;
	
	protected NewsRequest newsRequest;
	
	CategoryRequest categoryRequest;
	
	Set<RoleEntity> roles;
	
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

	
	protected void saveCategoryBefore() {
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
		
		
		CategoryEntity  newCategory = 	categoryRepo.save(categoryMapper.Request2Entity(categoryRequest));		
		newsRequest.setIdCategory(newCategory.getId());
	}
	
	protected void postUser() throws Exception, JsonProcessingException {
		userRequest = UserRequest.builder()
				.firstName("user")
				.lastName("user")
				.email("user@test.com")
				.password("12345678")
				.build();

		this.mockMvc
				.perform(MockMvcRequestBuilders.post(REGISTER_URL)
						.content(objectMapper.writeValueAsString(userRequest))
						.contentType(MediaType.APPLICATION_JSON));

	}

	protected String generateAdminToken() throws IOException, UnsupportedEncodingException, Exception, JsonProcessingException {
		buildAndRegisterAdmin();	
		
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
	

	protected String getAdminToken() throws IOException, UnsupportedEncodingException, Exception, JsonProcessingException {
		buildWithoutRegisterAdmin();	
		
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
	
	protected String generateUserToken() throws IOException, UnsupportedEncodingException, Exception, JsonProcessingException {
		buildAndRegisterUser();	
		
		String content = this.mockMvc
				.perform(post(AUTH_LOGIN_URL).contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								AuthRequest.builder()
								.email("user@test.com")
								.password("12345678")
								.build())))
				.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);		
	
		return JsonPath.read(content, "$.token");
	}
	
	protected void buildAndRegisterAdmin() throws IOException {
		userRequest = UserRequest.builder()
				.firstName("admin")
				.lastName("admin")
				.email("admin@test.com")
				.password("12345678")
				.build();
		authService.registerAdmin(userRequest);
	}
	
	protected void buildWithoutRegisterAdmin() throws IOException {
		userRequest = UserRequest.builder()
				.firstName("admin")
				.lastName("admin")
				.email("admin@test.com")
				.password("12345678")
				.build();
	}

	protected void buildAndRegisterUser() throws UsernameNotFoundException, IOException {
		userRequest = UserRequest.builder()
				.firstName("user")
				.lastName("user")
				.email("user@test.com")
				.password("12345678")
				.build();
		authService.register(userRequest);
	}
	
}
