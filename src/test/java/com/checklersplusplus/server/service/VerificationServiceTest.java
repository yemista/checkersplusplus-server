package com.checklersplusplus.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.VerifyAccountModel;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VerificationServiceTest {
	
	private static final String TEST_EMAIL = "test@test.com";
	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_USERNAME = "test";

	@Autowired
	private VerificationService verificationService;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<VerifyAccountModel> verifyAccountsToDelete = new ArrayList<>();
	
	@After
	public void cleanupDatabaseObjects() {
		accountsToDelete.forEach(account -> accountRepository.delete(account));
		verifyAccountsToDelete.forEach(verifyAccount -> verifyAccountRepository.delete(verifyAccount));
	}
	
	@Test
	public void canCreateVerificationCode() {
		UUID accountId = UUID.randomUUID();
		createVerificationCode(accountId);
		createVerificationCode(accountId);
		List<VerifyAccountModel> verifyAccountsBefore = verifyAccountRepository.getByAccountId(accountId);
		assertThat(verifyAccountsBefore.size()).isEqualTo(2);
		String verificationCode = createVerificationCode(accountId);
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getActiveByAccountId(accountId);
		assertThat(verifyAccount.isPresent()).isTrue();
		assertThat(verifyAccount.get().getAccountId()).isEqualTo(accountId);
		assertThat(verifyAccount.get().getActive()).isTrue();
		assertThat(verifyAccount.get().getVerificationCode()).isEqualTo(verificationCode);
		assertThat(verifyAccount.get().getCreated()).isNotNull();
	}
	
	@Test
	public void cannotVerifyAccountInvalidUsername() throws Exception {
		try {
			verificationService.verifyAccount("abc", "abcdef");
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Username not found.");
		}
	}
	
	@Test
	public void cannotVerifyAccountInvalidVerificationCode() throws Exception {
		createAccount(new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME));
		
		try {
			verificationService.verifyAccount(TEST_USERNAME, "abcdef");
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Invalid verification code. Check your email for the most recent code.");
		}
	}
	
	@Test
	public void canVerifyAccount() throws Exception {
		AccountModel account = createAccount(new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME));
		String verificationCode = verificationService.createVerificationCode(account.getAccountId());
		Optional<AccountModel> unverifiedAccount = accountRepository.getByUsernameIgnoreCase(TEST_USERNAME);
		assertThat(unverifiedAccount.isPresent()).isTrue();
		assertThat(unverifiedAccount.get().getVerified()).isNull();
		verificationService.verifyAccount(TEST_USERNAME, verificationCode);
		Optional<AccountModel> updatedAccount = accountRepository.getByUsernameIgnoreCase(TEST_USERNAME);
		assertThat(updatedAccount.isPresent()).isTrue();
		assertThat(updatedAccount.get().getVerified()).isNotNull();
	}
	
	private String createVerificationCode(UUID accountId) {
		String verificationCode = verificationService.createVerificationCode(accountId);
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getActiveByAccountId(accountId);
		assertThat(verifyAccount.isPresent()).isTrue();
		verifyAccountsToDelete.add(verifyAccount.get());
		return verificationCode;
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
