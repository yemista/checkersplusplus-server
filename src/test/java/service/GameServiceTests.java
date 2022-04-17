package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
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

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { HibernateConfig.class })
@ComponentScan( "com.checkersplusplus.service" )
public class GameServiceTests {

	private static final String DEFAULT_GAME_STATE = "";
	private static final String PENDING_STATUS = null;

	@Autowired
	private GameService gameService;
	
	@Autowired
	private AccountService accountService;
	
	@Test
	public void assertLogin() {
		accountService.createAccount("test5@test.com", "test", "test");
		User user = accountService.getAccount("test5@test.com");
		Login login = accountService.login("test@test.com");
		assertEquals(user.getId(), login.getUserId());
		assertNotNull(login.getSessionId());
		assertTrue(login.getSessionId().length() > 0);
	}
	
	@Test
	public void assertCreateGame() {
		accountService.createAccount("test6@test.com", "test", "test");
		Login login = accountService.login("test6@test.com");
		Game game = gameService.createGame(login.getSessionId());
		assertEquals(game.getState(), DEFAULT_GAME_STATE);
		assertNotNull(game.getId());
		assertEquals(game.getStatus(), PENDING_STATUS);
		assertNotEquals(game.getNextToAct(), login.getUserId());
	}
	
	@Test
	public void assertHasActiveGame() {
		accountService.createAccount("test7@test.com", "test", "test");
		Login login = accountService.login("test6@test.com");
		boolean hasActiveGame = gameService.hasActiveGame(login.getSessionId());
		assertFalse(hasActiveGame);
		Game game = gameService.createGame(login.getSessionId());
		hasActiveGame = gameService.hasActiveGame(login.getSessionId());
		assertTrue(hasActiveGame);
	}
	
}
