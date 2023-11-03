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
import com.checklersplusplus.server.entities.request.VerifyAccount;
import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.VerificationService;
import com.checklersplusplus.server.service.mail.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AccountController.class)
public class VerifyAccountTest {
	
	private static final String TEST_USERNAME = "test";
	private static final String TEST_VERIFICATION_CODE = "ABCDEF";

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
	public void canVerifyAccount() throws Exception {
		VerifyAccount verifyAccount = new VerifyAccount();
		verifyAccount.setUsername(TEST_USERNAME);
		verifyAccount.setVerificationCode(TEST_VERIFICATION_CODE);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Account verified.");
	}

	@Test
	public void cannotVerifyAccount() throws Exception {
		VerifyAccount verifyAccount = new VerifyAccount();
		verifyAccount.setUsername(TEST_USERNAME);
		verifyAccount.setVerificationCode(TEST_VERIFICATION_CODE);
		Mockito.doThrow(new CheckersPlusPlusServerException("Failed to verify account. Please enter the most recent verification code."))
			.when(verificationService).verifyAccount(any(), any());
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Failed to verify account. Please enter the most recent verification code.");
	}
	
	@Test
	public void cannotVerifyAccountWithMissingUsername() throws Exception {
		VerifyAccount verifyAccount = new VerifyAccount();
		verifyAccount.setUsername(null);
		verifyAccount.setVerificationCode(TEST_VERIFICATION_CODE);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(),"Username is required.");
	}
	
	@Test
	public void cannotVerifyAccountWithMissingVerificationCode() throws Exception {
		VerifyAccount verifyAccount = new VerifyAccount();
		verifyAccount.setUsername(TEST_USERNAME);
		verifyAccount.setVerificationCode(null);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(),"Verification code is required.");
	}
}
