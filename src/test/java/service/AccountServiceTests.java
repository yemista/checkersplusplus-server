package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.User;

import config.HibernateConfig;
import util.UserNameTestUtil;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { HibernateConfig.class })
@ComponentScan( "com.checkersplusplus.service" )
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
}
