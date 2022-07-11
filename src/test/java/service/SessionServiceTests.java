package service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.SessionService;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.Session;
import com.checkersplusplus.service.models.User;

import config.TestJpaConfig;
import util.UserNameTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  classes = { TestJpaConfig.class }, 
  loader = AnnotationConfigContextLoader.class)
public class SessionServiceTests {

	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private AccountService accountService;

	@Test
	public void assertInvalidateSession() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		Session activeSession = sessionService.getLatestActiveSessionByUserId(login.getUserId());
		assertNotNull(activeSession);
		sessionService.invalidateSession(login.getSessionId());
		Session inactiveSession = sessionService.getLatestActiveSessionByUserId(login.getUserId());
		assertNull(activeSession);
	}
}
