package com.checklersplusplus.server.controller;

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

import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.request.VerifyAccount;
import com.checklersplusplus.server.entities.response.Account;
import com.checklersplusplus.server.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AccountController.class)
public class AccountControllerTest {

	private static final String TEST_EMAIL = "test@test.com";
	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_USERNAME = "test";
	private static final String TEST_VERIFICATION_CODE = "ABCDEF";

	@MockBean
	private AccountService accountService;
	
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
		verifyAccount.setUsername(TEST_EMAIL);
		verifyAccount.setVerificationCode(TEST_VERIFICATION_CODE);
		Mockito.doThrow(new Exception()).when(accountService).verifyAccount(any());
		mockMvc.perform(post("/checkersplusplus/api/account/verify").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(verifyAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Failed to verify account. Please enter the most recent verification code."))
							.andDo(print());
	}
	
	@Test
	public void canCreateAccount() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isOk())
							.andExpect(content().string("Account created successfully. Please check your email for the verification code."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithoutMatchingPasswords() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_USERNAME, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Password and confirmation password do not match."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithDuplicateUsername() throws Exception {
		Mockito.when(accountService.findByUsername(TEST_USERNAME)).thenReturn(new Account());
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Username is already in use."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithDuplicateEmail() throws Exception {
		Mockito.when(accountService.findByEmail(TEST_EMAIL)).thenReturn(new Account());
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Email address is already in use."))
							.andDo(print());
	}
}
