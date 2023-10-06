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
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.response.Account;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AccountController.class)
public class CreateAccountTest {

	private static final String TEST_EMAIL = "test@test.com";
	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_USERNAME = "test";
	
	@MockBean
	private AccountService accountService;
	
	@MockBean
	private EmailService emailService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void canCreateAccount() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isOk())
							.andExpect(content().string("Account created successfully. Please check your email for the verification code. If you do not see it check your spam folder."))
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
		Mockito.when(accountService.findByUsername(any())).thenReturn(new Account());
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
	
	@Test
	public void cannotCreateAccountWithMissingEmail() throws Exception {
		CreateAccount createAccount = new CreateAccount(null, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Email is required."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithMissingPassword() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, null, TEST_PASSWORD, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Password is required."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithMissingConfirmationPassword() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, null, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Confirmation password is required."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithMissingUsername() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, null);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Username is required."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithInvalidEmail() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_PASSWORD, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("The provided email is not a valid email."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithInvalidUsername() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, "a");
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Username must be from 3 to 20 characters."))
							.andDo(print());
	}
	
	@Test
	public void cannotCreateAccountWithInvalidPassword() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, "abc", "abc", TEST_USERNAME);
		mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andExpect(content().string("Password must be 10 characters long and combination of uppercase letters, lowercase letters, numbers."))
							.andDo(print());
	}
}
