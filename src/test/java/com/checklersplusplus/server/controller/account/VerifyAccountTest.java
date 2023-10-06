package com.checklersplusplus.server.controller.account;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

import com.checklersplusplus.server.controller.AccountController;
import com.checklersplusplus.server.entities.request.VerifyAccount;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.EmailService;
import com.checklersplusplus.server.service.VerificationService;
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
		mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isOk())
							.andExpect(content().string("Account verified."))
							.andDo(print());
	}

	@Test
	public void cannotVerifyAccount() throws Exception {
		VerifyAccount verifyAccount = new VerifyAccount();
		verifyAccount.setUsername(TEST_USERNAME);
		verifyAccount.setVerificationCode(TEST_VERIFICATION_CODE);
		Mockito.doThrow(new Exception()).when(verificationService).verifyAccount(any(), any());
		mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Failed to verify account. Please enter the most recent verification code."))
							.andDo(print());
	}
	
	@Test
	public void cannotVerifyAccountWithMissingUsername() throws Exception {
		VerifyAccount verifyAccount = new VerifyAccount();
		verifyAccount.setUsername(null);
		verifyAccount.setVerificationCode(TEST_VERIFICATION_CODE);
		mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Username is required."))
							.andDo(print());
	}
	
	@Test
	public void cannotVerifyAccountWithMissingVerificationCode() throws Exception {
		VerifyAccount verifyAccount = new VerifyAccount();
		verifyAccount.setUsername(TEST_USERNAME);
		verifyAccount.setVerificationCode(null);
		mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Verification code is required."))
							.andDo(print());
	}
}
