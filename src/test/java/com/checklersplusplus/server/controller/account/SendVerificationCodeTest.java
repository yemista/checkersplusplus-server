package com.checklersplusplus.server.controller.account;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.checklersplusplus.server.entities.request.Username;
import com.checklersplusplus.server.entities.response.Account;
import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.VerificationService;
import com.checklersplusplus.server.service.mail.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AccountController.class)
public class SendVerificationCodeTest {

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
	public void canSendVerificationCode() throws Exception {
		Mockito.when(accountService.findByUsername(any())).thenReturn(new Account());
		Username username = new Username(TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/sendVerification").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(username)))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Please check your email for the verification code. If you do not see it check your spam folder.");
	}
	
	@Test
	public void cannotSendVerificationCodeWithInvalidUsername() throws Exception {
		Mockito.when(accountService.findByUsername(any())).thenReturn(null);
		Username username = new Username(TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/sendVerification").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(username)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Username not found.");
	}
	
	@Test
	public void cannotSendVerificationCodeWithMissingUsername() throws Exception {
		Username username = new Username(null);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/sendVerification").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(username)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Username not found.");
	}
}
