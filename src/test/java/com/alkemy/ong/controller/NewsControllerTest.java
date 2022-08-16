package com.alkemy.ong.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.junit.Before;
import org.junit.Test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.alkemy.ong.context.ContextTest;
import com.fasterxml.jackson.core.JsonProcessingException;


public class NewsControllerTest extends ContextTest {
	
	private static final String POST_NEWS_URL = "/news";

	private static final String GET_NEWS_BY_ID_URL = "/news/{id}";
	
    
	@Test
	public void should_return_news() throws Exception, IOException {
		saveCategoryBefore();
		String token = generateAdminToken();
		this.mockMvc
				.perform(MockMvcRequestBuilders.post(POST_NEWS_URL)
						.header(HttpHeaders.AUTHORIZATION, BEARER + token )
						.content(objectMapper.writeValueAsString(newsRequest))
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(MockMvcResultMatchers.status().isCreated());
	}
	
	@Test
	public void should_return_news_by_id() throws Exception, IOException {
		String token = getAdminToken();
		this.mockMvc
				.perform(MockMvcRequestBuilders.get(GET_NEWS_BY_ID_URL, 1L)
						.header(HttpHeaders.AUTHORIZATION, BEARER + token))
				.andExpect(MockMvcResultMatchers.status().isOk())
		        .andExpect(MockMvcResultMatchers.jsonPath("$.name").isNotEmpty())
        		.andExpect(MockMvcResultMatchers.jsonPath("$.content").isNotEmpty())
    	        .andExpect(MockMvcResultMatchers.jsonPath("$.image").isNotEmpty());	
	}
	
}
