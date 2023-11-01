package com.checklersplusplus.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.RatingRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.response.Account;
import com.checklersplusplus.server.entities.response.Session;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.RatingModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.model.VerifyAccountModel;
import com.checklersplusplus.server.util.CryptoUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountServiceTest {

	private static final String TEST_EMAIL = "test@test.com";
	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_USERNAME = "test";
	private static final String TEST_VERIFICATION_CODE = "ABCDEF";
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<VerifyAccountModel> verifyAccountsToDelete = new ArrayList<>();
	
	@After
	public void cleanupDatabaseObjects() {
		accountsToDelete.forEach(account -> accountRepository.delete(account));
		verifyAccountsToDelete.forEach(verifyAccount -> verifyAccountRepository.delete(verifyAccount));
	}
	
	@Test
	public void testFindByUsername() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccount);
		Account account = accountService.findByUsername(TEST_USERNAME);
		assertEquals(account.getAccountId(), accountModel.getAccountId());
	}
	
	@Test
	public void testFindByEmail() throws Exception {
		CreateAccount createAccount = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccount);
		Account account = accountService.findByEmail(TEST_EMAIL);
		assertEquals(account.getAccountId(), accountModel.getAccountId());
	}
	
	@Test
	public void testCannotCreateAccountWithDuplicateUsername() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		createAccount(createAccountInput);
		CreateAccount createAccountInput2 = new CreateAccount(TEST_EMAIL + "m", TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		
		try {
			createAccount(createAccountInput2);
			fail();
		} catch(DataIntegrityViolationException e) {
			assertTrue(e.getMessage().contains("duplicate key value violates unique constraint"));
		}
	}
	
	@Test
	public void testCannotCreateAccountWithDuplicateEmail() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		createAccount(createAccountInput);
		CreateAccount createAccountInput2 = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME + "1");
		
		try {
			createAccount(createAccountInput2);
			fail();
		} catch(DataIntegrityViolationException e) {
			assertTrue(e.getMessage().contains("duplicate key value violates unique constraint"));
		}
	}
	
	@Test
	public void testCreateAccount() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccountInput);
		assertNotNull(accountModel.getAccountId());
		assertEquals(accountModel.getEmail(), TEST_EMAIL);
		assertEquals(accountModel.getPassword(), CryptoUtil.encryptPassword(TEST_PASSWORD));
		assertEquals(accountModel.getUsername(), TEST_USERNAME);
		assertNull(accountModel.getVerified());
		
		Optional<RatingModel> rating = ratingRepository.findByAccountId(accountModel.getAccountId());
		assertThat(rating.isPresent()).isTrue();
		assertThat(rating.get().getRating()).isEqualTo(800);
	}
	
	@Test
	public void testCannotLoginInvalidAccount() throws Exception {
		try {
			accountService.login(TEST_USERNAME, TEST_PASSWORD);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Failed to login. Account not found.");
		}
	}
	
	@Test
	public void testCannotLoginUnverifiedAccount() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccountInput);
		
		try {
			accountService.login(TEST_USERNAME, TEST_PASSWORD);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Account not verified.");
		}
	}
	
	@Test
	public void testCanLogin() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccountInput);
		accountModel.setVerified(LocalDateTime.now());
		accountRepository.save(accountModel);
		GameEventModel gameEvent = new GameEventModel();
		gameEvent.setActive(true);
		gameEvent.setEvent("");
		gameEvent.setEventRecipientAccountId(accountModel.getAccountId());
		UUID gameId = UUID.randomUUID();
		gameEvent.setGameId(gameId);
		gameEventRepository.save(gameEvent);
		Optional<GameEventModel> fetchedGameEvent = gameEventRepository.findActiveEventForAccountIdAndGameId(accountModel.getAccountId(), gameId);
		assertThat(fetchedGameEvent.isPresent()).isTrue();
		Session session = accountService.login(TEST_USERNAME, TEST_PASSWORD);
		Optional<GameEventModel> reFetchedGameEvent = gameEventRepository.findActiveEventForAccountIdAndGameId(accountModel.getAccountId(), gameId);
		assertThat(reFetchedGameEvent.isPresent()).isFalse();
		assertThat(session.getSessionId()).isNotNull();
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(session.getSessionId());
		assertThat(sessionModel.isPresent()).isTrue();
		assertThat(sessionModel.get().getSessionId()).isEqualTo(session.getSessionId());
		assertThat(session.getMessage()).isEqualTo("Login successful.");
	}
	
	@Test
	public void testCannotResetPasswordInvalidAccount() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccountInput);
		
		try {
			accountService.resetPassword("abc", "abc", TEST_PASSWORD);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Username not found.");
		}
	}
	
	@Test
	public void testCannotResetPasswordInvalidVerificationCodeAccount() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccountInput);
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getActiveByAccountId(accountModel.getAccountId());
		assertTrue(verifyAccount.isPresent());
		verifyAccount.get().setVerificationCode("123456");
		verifyAccountRepository.save(verifyAccount.get());
		
		try {
			accountService.resetPassword(TEST_USERNAME, TEST_VERIFICATION_CODE, TEST_PASSWORD);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Invalid verification code. Check your email for the most recent code.");
		}
	}
	
	@Test
	public void testCanResetPassword() throws Exception {
		CreateAccount createAccountInput = new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME);
		AccountModel accountModel = createAccount(createAccountInput);
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getActiveByAccountId(accountModel.getAccountId());
		assertTrue(verifyAccount.isPresent());
		verifyAccount.get().setVerificationCode(TEST_VERIFICATION_CODE);
		verifyAccountRepository.save(verifyAccount.get());
		String newPassword = "NewPassword1";
		accountService.resetPassword(TEST_USERNAME, TEST_VERIFICATION_CODE, newPassword);
		Optional<AccountModel> updatedAccount = accountRepository.getByUsername(TEST_USERNAME);
		assertThat(updatedAccount.isPresent()).isTrue();
		assertThat(updatedAccount.get().getPassword()).isEqualTo(CryptoUtil.encryptPassword(newPassword));
	}
	
	private AccountModel createAccount(CreateAccount createAccount) throws Exception {
		accountService.createAccount(createAccount);
		Optional<AccountModel> accountModel = accountRepository.getByEmail(createAccount.getEmail());
		assertTrue(accountModel.isPresent());
		accountsToDelete.add(accountModel.get());
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getActiveByAccountId(accountModel.get().getAccountId());
		assertTrue(verifyAccount.isPresent());
		verifyAccountsToDelete.add(verifyAccount.get());
		return accountModel.get();
	}
}
