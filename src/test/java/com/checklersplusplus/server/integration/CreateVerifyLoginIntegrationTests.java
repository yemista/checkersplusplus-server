package com.checklersplusplus.server.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.request.Login;
import com.checklersplusplus.server.entities.request.VerifyAccount;
import com.checklersplusplus.server.entities.response.Session;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.model.VerifyAccountModel;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CreateVerifyLoginIntegrationTests {
	
	private static final String TEST_EMAIL = "test@test.com";
	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_USERNAME = "test";

	@Autowired
	private TestRestTemplate restTemplate;
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private SessionRepository sessionRepository;

	@Test
	public void createVerifyLogin() {
		CreateAccount createAccountParams = new CreateAccount();
		createAccountParams.setEmail(TEST_EMAIL);
		createAccountParams.setPassword(TEST_PASSWORD);
		createAccountParams.setConfirmPassword(TEST_PASSWORD);
		createAccountParams.setUsername(TEST_USERNAME);
		ResponseEntity<String> createAccountResponse = restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/account/create", createAccountParams, String.class);
		assertEquals(createAccountResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(createAccountResponse.getBody(), "Account created successfully. Please check your email for the verification code. If you do not see it check your spam folder.");
		Optional<VerifyAccountModel> verifyAccountModel = verifyAccountRepository.getActiveByUsername(TEST_USERNAME);
		assertTrue(verifyAccountModel.isPresent());
		VerifyAccount verifyAccountParams = new VerifyAccount();
		verifyAccountParams.setUsername(TEST_USERNAME);
		verifyAccountParams.setVerificationCode(verifyAccountModel.get().getVerificationCode());
		ResponseEntity<String> verifyAccountResponse = restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/account/verify", verifyAccountParams, String.class);
		assertEquals(verifyAccountResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(verifyAccountResponse.getBody(), "Account verified.");
		Login loginParams = new Login();
		loginParams.setUsername(TEST_USERNAME);
		loginParams.setPassword(TEST_PASSWORD);
		ResponseEntity<Session> loginResponse = restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/account/login", loginParams, Session.class);
		assertEquals(loginResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(loginResponse.getBody().getMessage(), "Login successful.");
		assertNotNull(loginResponse.getBody().getSessionId());
		Optional<SessionModel> sessionModel = sessionRepository.getBySessionId(loginResponse.getBody().getSessionId());
		assertTrue(sessionModel.isPresent());
	}
}
