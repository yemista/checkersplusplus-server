package com.checklersplusplus.server.controller.account;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.checklersplusplus.server.controller.AccountController;
import com.checklersplusplus.server.entities.request.Login;
import com.checklersplusplus.server.entities.response.Session;
import com.checklersplusplus.server.exception.AccountNotVerifiedException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.VerificationService;
import com.checklersplusplus.server.service.mail.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AccountController.class)
public class LoginTest {

	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_USERNAME = "test";
	
	@MockBean
	private AccountService accountService;
	
	@MockBean
	private EmailService emailService;
	
	@MockBean
	private VerificationService verificationService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void canLogin() throws Exception {
		Session sessionResponse = new Session(UUID.randomUUID(), null);
		sessionResponse.setMessage("Login successful.");
		Mockito.when(accountService.login(any(), any())).thenReturn(sessionResponse);
		Login login = new Login(TEST_USERNAME, TEST_PASSWORD);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(login)))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		Session response = objectMapper.readValue(contentAsString, Session.class);
		assertEquals(response.getMessage(), "Login successful.");
	}
	
	@Test
	public void cannotLoginWithUnverifiedAccount() throws Exception {
		Mockito.doThrow(new AccountNotVerifiedException()).when(accountService).login(any(), any());
		Login login = new Login(TEST_USERNAME, TEST_PASSWORD);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(login)))
							.andExpect(status().isForbidden())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		Session response = objectMapper.readValue(contentAsString, Session.class);
		assertEquals(response.getMessage(), "Account not verified.");
	}
	
	@Test
	public void cannotLoginWithInvalidUsername() throws Exception {
		Mockito.doThrow(new CheckersPlusPlusServerException("Failed to login. Account not found.")).when(accountService).login(any(), any());
		Login login = new Login(TEST_USERNAME, TEST_PASSWORD);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(login)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		Session response = objectMapper.readValue(contentAsString, Session.class);
		assertEquals(response.getMessage(), "Failed to login. Account not found.");
	}
}
