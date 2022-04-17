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
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.User;

import config.HibernateConfig;
import util.UserNameTestUtil;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { HibernateConfig.class })
@ComponentScan( "com.checkersplusplus.service" )
public class GameServiceTests {

	private static final String DEFAULT_GAME_STATE = "NX|OEOEOEOEEOEOEOEOOEOEOEOEEEEEEEEEEEEEEEEEEXEXEXEXXEXEXEXEEXEXEXEX";
	private static final String PENDING_STATUS = "PENDING";

	@Autowired
	private GameService gameService;
	
	@Autowired
	private AccountService accountService;
	
	@Test
	public void assertLogin() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		assertEquals(user.getId(), login.getUserId());
		assertNotNull(login.getSessionId());
		assertTrue(login.getSessionId().length() > 0);
	}
	
	@Test
	public void assertCreateGame() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		Game game = gameService.createGame(login.getSessionId());
		assertEquals(game.getState(), DEFAULT_GAME_STATE);
		assertNotNull(game.getId());
		assertEquals(game.getStatus(), PENDING_STATUS);
		assertEquals(game.getNextToAct(), user.getId());
	}
	
	@Test
	public void assertHasActiveGame() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		boolean hasActiveGame = gameService.hasActiveGame(login.getSessionId());
		assertFalse(hasActiveGame);
		Game game = gameService.createGame(login.getSessionId());
		hasActiveGame = gameService.hasActiveGame(login.getSessionId());
		assertTrue(hasActiveGame);
	}
	
}
