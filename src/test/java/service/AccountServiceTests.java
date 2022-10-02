package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.models.EmailVerification;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.User;

import config.TestJpaConfig;
import util.UserNameTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  classes = { TestJpaConfig.class }, 
  loader = AnnotationConfigContextLoader.class)
public class AccountServiceTests {
	
	@Autowired
	private AccountService accountService;
	
	@Test
	public void assertCreateUser() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		User createdUser = accountService.getAccount(email);
		assertNotNull(createdUser);
		assertEquals(createdUser.getAlias(), userName);
	}
	
	@Test
	public void assertIsAliasValid() {
		assertFalse(accountService.isAliasValid("aa"));
		assertFalse(accountService.isAliasValid("aaaaaaaaaaaaaaa"));
		assertTrue(accountService.isAliasValid("aaa"));
	}
	
	@Test
	public void assertIsPasswordSafe() {
		assertFalse(accountService.isPasswordSafe("aaaaaaa"));
		assertFalse(accountService.isPasswordSafe("aaaaaaaaaaaaaaaaaaaaa"));
		assertTrue(accountService.isPasswordSafe("aaaaaaa12"));
		assertFalse(accountService.isPasswordSafe("aaaaaaa^^^"));
	}
	
	@Test
	public void assertIsLoginValid() {
		String email = "test2@test.com";
		String password = "test";
		accountService.createAccount(email, password, "test2");
		assertTrue(accountService.isLoginValid(email, password));
		assertFalse(accountService.isLoginValid(email + "1", password));
		assertFalse(accountService.isLoginValid(email + "1", password + "1"));
	}
	
	@Test
	public void assertLogin() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		assertNotNull(accountService.login(email));
		assertNull(accountService.login(email + "1"));
	}
	
	@Test
	public void assertGetVerificationCode() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		EmailVerification verification = accountService.createEmailVerificationCode(email);
		String code = accountService.getActiveVerificationCode(email);
		assertEquals(code, verification.getCode());
	}
	
	@Test
	public void assertVerifyEmail() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		EmailVerification verification = accountService.createEmailVerificationCode(email);
		String code = accountService.getActiveVerificationCode(email);
		assertEquals(code, verification.getCode());
		User account = accountService.getAccount(email);
		assertEquals(account.getVerified(), 0);
		accountService.verifyAccount(email);
		String codeAfterVerification = accountService.getActiveVerificationCode(email);
		assertNull(codeAfterVerification);
		User verifiedAccount = accountService.getAccount(email);
		assertEquals(verifiedAccount.getVerified(), 1);
	}
}
