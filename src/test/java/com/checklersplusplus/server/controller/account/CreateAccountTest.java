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
import com.checklersplusplus.server.entities.internal.NewAccount;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.response.Account;
import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.VerificationService;
import com.checklersplusplus.server.service.mail.EmailService;
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
	
	@MockBean
	private VerificationService verificationService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void canCreateAccount() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		Mockito.when(accountService.createAccount(any())).thenReturn(new NewAccount(UUID.randomUUID(), "123456"));
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Account created successfully. Please check your email for the verification code. If you do not see it check your spam folder.");
	}
	
	@Test
	public void cannotCreateAccountWithoutMatchingPasswords() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_USERNAME, TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Password and confirmation password do not match.");
	}
	
	@Test
	public void cannotCreateAccountWithDuplicateUsername() throws Exception {
		Mockito.when(accountService.findByUsername(any())).thenReturn(new Account());
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Username is already in use.");
	}
	
	@Test
	public void cannotCreateAccountWithDuplicateEmail() throws Exception {
		Mockito.when(accountService.findByEmail(TEST_EMAIL)).thenReturn(new Account());
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Email address is already in use.");
	}
	
	@Test
	public void cannotCreateAccountWithMissingEmail() throws Exception {
		CreateAccount createAccount = new CreateAccount(null, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Email is required.");
	}
	
	@Test
	public void cannotCreateAccountWithMissingPassword() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, null, TEST_PASSWORD, TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Password is required.");
	}
	
	@Test
	public void cannotCreateAccountWithMissingConfirmationPassword() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, null, TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Confirmation password is required.");
	}
	
	@Test
	public void cannotCreateAccountWithMissingUsername() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, null);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Username is required.");
	}
	
	@Test
	public void cannotCreateAccountWithInvalidEmail() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_PASSWORD, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "The provided email is not a valid email.");
	}
	
	@Test
	public void cannotCreateAccountWithInvalidUsername() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, "a");
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Username must be from 3 to 20 characters.");
	}
	
	@Test
	public void cannotCreateAccountWithInvalidPassword() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, "abc", "abc", TEST_USERNAME);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createAccount)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Password must be 8 characters long and combination of uppercase letters, lowercase letters, numbers.");
	}
}
