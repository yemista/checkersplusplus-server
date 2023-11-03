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
import com.checklersplusplus.server.entities.request.ResetPassword;
import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;
import com.checklersplusplus.server.exception.InvalidVerificationCodeException;
import com.checklersplusplus.server.exception.UsernameNotFoundException;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.VerificationService;
import com.checklersplusplus.server.service.mail.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = AccountController.class)
public class ResetPasswordTest {
	
	private static final String TEST_USERNAME = "test";
	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_VERIFICATION_CODE = "TEST12";

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
	public void canResetPassword() throws Exception {
		ResetPassword resetPassword = new ResetPassword();
		resetPassword.setUsername(TEST_USERNAME);
		resetPassword.setPassword(TEST_PASSWORD);
		resetPassword.setConfirmPassword(TEST_PASSWORD);
		resetPassword.setVerificationCode(TEST_VERIFICATION_CODE);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(resetPassword)))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Password reset successful.");
	}
	
	@Test
	public void cannotResetPasswordWithInvalidUsername() throws Exception {
		ResetPassword resetPassword = new ResetPassword();
		resetPassword.setUsername(TEST_USERNAME);
		resetPassword.setPassword(TEST_PASSWORD);
		resetPassword.setConfirmPassword(TEST_PASSWORD);
		resetPassword.setVerificationCode(TEST_VERIFICATION_CODE);
		Mockito.doThrow(new UsernameNotFoundException()).when(accountService).resetPassword(any(), any(), any());
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(resetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Username not found.");
	}
	
	@Test
	public void cannotResetPasswordWithInvalidVerificationCode() throws Exception {
		ResetPassword resetPassword = new ResetPassword();
		resetPassword.setUsername(TEST_USERNAME);
		resetPassword.setPassword(TEST_PASSWORD);
		resetPassword.setConfirmPassword(TEST_PASSWORD);
		resetPassword.setVerificationCode(TEST_VERIFICATION_CODE);
		Mockito.doThrow(new InvalidVerificationCodeException()).when(accountService).resetPassword(any(), any(), any());
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(resetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Invalid verification code. Check your email for the most recent code.");
	}
	
	@Test
	public void cannotResetPasswordWithMissingPassword() throws Exception {
		ResetPassword ResetPassword = new ResetPassword(TEST_USERNAME, null, TEST_PASSWORD, TEST_VERIFICATION_CODE);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ResetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Password is required.");
	}
	
	@Test
	public void cannotResetPasswordWithMissingConfirmationPassword() throws Exception {
		ResetPassword ResetPassword = new ResetPassword(TEST_USERNAME, TEST_PASSWORD, null, TEST_VERIFICATION_CODE);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ResetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(),"Confirmation password is required.");
	}
	
	@Test
	public void cannotResetPasswordWithMissingUsername() throws Exception {
		ResetPassword ResetPassword = new ResetPassword(null, TEST_PASSWORD, TEST_PASSWORD, TEST_VERIFICATION_CODE);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ResetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Username is required.");
	}
	
	@Test
	public void cannotResetPasswordWithMissingVerificationCode() throws Exception {
		ResetPassword ResetPassword = new ResetPassword(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD, null);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ResetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(),"Verification code is required.");
	}
	
	@Test
	public void cannotResetPasswordWithInvalidVerificationCodeParam() throws Exception {
		ResetPassword ResetPassword = new ResetPassword(TEST_USERNAME, TEST_PASSWORD, TEST_PASSWORD, "abc");
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ResetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Invalid verification code.");
	}
	
	@Test
	public void cannotResetPasswordWithInvalidPassword() throws Exception {
		ResetPassword ResetPassword = new ResetPassword(TEST_USERNAME, "abc", "abc", TEST_VERIFICATION_CODE);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/account/resetPassword").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(ResetPassword)))
							.andExpect(status().isBadRequest())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Password must be 8 characters long and combination of uppercase letters, lowercase letters, numbers.");
	}
}
