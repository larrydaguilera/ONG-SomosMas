package com.alkemy.ong.controller;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.print.attribute.standard.Media;
import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.alkemy.ong.auth.utility.RoleEnum;

import com.alkemy.ong.models.entity.RoleEntity;
import com.alkemy.ong.models.entity.UserEntity;
import com.alkemy.ong.models.mapper.UserMapper;
import com.alkemy.ong.models.request.AuthRequest;
import com.alkemy.ong.models.request.UserRequest;
import com.alkemy.ong.repository.RoleRepository;
import com.alkemy.ong.repository.UserRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
@SpringBootTest

@AutoConfigureMockMvc
@Transactional
@RunWith(SpringRunner.class)

public class AuthControllerTest   {
	
    protected static final String BEARER = "Bearer ";
    private static final String REGISTER_URL = "/auth/register";
    private static final String AUTH_LOGIN_URL = "/auth/login";
    private static final String AUTH_ME_URL = "/auth/me";

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    public MockMvc mockMvc;
    
    @Autowired
    public UserMapper mapper;
     
    @Autowired
    public UserRepository userRepo; 
     
    @Autowired
    public RoleRepository roleRepo;
    
    Set<RoleEntity> roles; 
    
   	UserRequest	 userRequest = new UserRequest("test", "test", "test@test.com","12345678", null);
   	
   	AuthRequest	 authRequest = new AuthRequest("test@test.com","12345678");  
   	
   
   	
   	

    @Test
    public void should_return_user_created() throws Exception {
  
    	mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .content(objectMapper.writeValueAsString(userRequest))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isCreated());
    }
    
 

	@Test
    public void should_return_bad_request() throws Exception {
    	
    	UserRequest	 userRequest = new UserRequest("test", "test", null,"12345678", null);
    	
    	this.mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .content(objectMapper.writeValueAsString(userRequest))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
    
    @Test
    public void should_return_conflict() throws Exception ,IOException {
    	
    	UserEntity user = mapper.toUserEntity(userRequest, roles); 
    	userRepo.save(user);
    	this.mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .content(objectMapper.writeValueAsString(userRequest))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isConflict());
    }
    
    @Test
    public void should_return_200ok() throws Exception ,IOException {
    	UserEntity user = mapper.toUserEntity(userRequest, roles); 
    	userRepo.save(user);
    	this.mockMvc.perform(MockMvcRequestBuilders.post(AUTH_LOGIN_URL)
        .content(objectMapper.writeValueAsString(AuthRequest.builder()
        		.email(authRequest.getEmail())
        		.password(authRequest.getPassword()).build()))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    public void should_return_ok_false_when_password_is_invalid() throws Exception ,IOException {
    	UserEntity user = mapper.toUserEntity(userRequest, roles); 
    	userRepo.save(user);
    	this.mockMvc.perform(MockMvcRequestBuilders.post(AUTH_LOGIN_URL)
        .content(objectMapper.writeValueAsString(AuthRequest.builder()
        		.email(authRequest.getEmail())
        		.password("falsopass").build()))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.ok", equalTo(false)));
    }
    
    @Test
    public void should_return_user_details() throws Exception ,IOException {
    	
    	mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_URL)
        .content(objectMapper.writeValueAsString(userRequest))
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isCreated());
        
        String content = mockMvc.perform(post(AUTH_LOGIN_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(AuthRequest.builder()
                        .email("test@test.com")
                        .password("12345678")
                        .build()))).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        String token = JsonPath.read(content, "$.token");
        
    	UserEntity user = mapper.toUserEntity(userRequest, roles); 
    	this.mockMvc.perform(MockMvcRequestBuilders.get(AUTH_ME_URL)
    			.header(HttpHeaders.AUTHORIZATION, BEARER + token)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.firstName", equalTo(user.getFirstName())))
        .andExpect(jsonPath("$.lastName", equalTo(user.getLastName())))
        .andExpect(jsonPath("$.email", equalTo(user.getEmail())))
        .andExpect(status().isOk());

    }
	

}
