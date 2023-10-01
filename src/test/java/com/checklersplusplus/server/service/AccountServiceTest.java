package com.checklersplusplus.server.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.Account;
import com.checklersplusplus.server.entities.CreateAccount;
import com.checklersplusplus.server.model.AccountModel;
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
	private VerifyAccountRepository verifyAccountRepository;
	
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
	}
	
	private AccountModel createAccount(CreateAccount createAccount) throws Exception {
		accountService.createAccount(createAccount);
		Optional<AccountModel> accountModel = accountRepository.getByEmail(TEST_EMAIL);
		assertTrue(accountModel.isPresent());
		accountsToDelete.add(accountModel.get());
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getByAccountId(accountModel.get().getAccountId());
		assertTrue(verifyAccount.isPresent());
		verifyAccountsToDelete.add(verifyAccount.get());
		return accountModel.get();
	}
}
