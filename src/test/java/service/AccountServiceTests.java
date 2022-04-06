package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.models.User;

import config.HibernateConfig;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { HibernateConfig.class })
@ComponentScan( "com.checkersplusplus.service" )
public class AccountServiceTests {
	
	@Autowired
	private AccountService accountService;
	
	@Test
	public void assertCreateUser() {
		accountService.createAccount("test", "test", "test");
		User createdUser = accountService.getAccount("test");
		assertNotNull(createdUser);
		assertEquals(createdUser.getAlias(), "test");
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
}
