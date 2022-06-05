package service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.checkersplusplus.exceptions.CannotJoinGameException;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.OpenGames;
import com.checkersplusplus.service.models.User;

import config.TestJpaConfig;
import util.UserNameTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  classes = { TestJpaConfig.class }, 
  loader = AnnotationConfigContextLoader.class)
public class GameServiceTests {

	private static final String DEFAULT_GAME_STATE = "NX|OEOEOEOEEOEOEOEOOEOEOEOEEEEEEEEEEEEEEEEEEXEXEXEXXEXEXEXEEXEXEXEX";

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
	public void assertCreateGame() throws Exception {
		Login login = createUserForTest();
		Game game = gameService.createGame(login.getSessionId());
		assertEquals(game.getState(), DEFAULT_GAME_STATE);
		assertNotNull(game.getId());
		assertEquals(game.getStatus(), GameStatus.PENDING);
		assertEquals(game.getRedId(), login.getUserId());
	}
	
	@Test
	public void assertHasActiveGame() throws Exception {
		Login login = createUserForTest();
		boolean hasActiveGame = gameService.getActiveGame(login.getSessionId()) != null;
		assertFalse(hasActiveGame);
		Game game = gameService.createGame(login.getSessionId());
		hasActiveGame = gameService.getActiveGame(login.getSessionId()) != null;
		assertTrue(hasActiveGame);
	}
	
	@Test
	public void assertJoinGame() throws Exception {
		Login login1 = createUserForTest();
		Game game = gameService.createGame(login1.getSessionId());
		Login login2 = createUserForTest();
		Game joinedGame = gameService.joinGame(login2.getSessionId(), game.getId());
		assertNotNull(joinedGame);
		assertTrue(StringUtils.isNotBlank(joinedGame.getId()));
	}
	
	@Test
	public void assertCannotJoinFullGame() throws Exception {
		Login login1 = createUserForTest();
		Game game = gameService.createGame(login1.getSessionId());
		Login login2 = createUserForTest();
		Game joinedGame = gameService.joinGame(login2.getSessionId(), game.getId());
		Login login3 = createUserForTest();
		
		try {
			gameService.joinGame(login3.getSessionId(), game.getId());
			fail();
		} catch (CannotJoinGameException e) {
			
		}
	}
	
	@Test
	public void assertCannotJoinGameIfAlreadyInGame() throws Exception {
		Login login1 = createUserForTest();
		Game game1 = gameService.createGame(login1.getSessionId());
		Login login2 = createUserForTest();
		Game game2 = gameService.createGame(login2.getSessionId());
		Login login3 = createUserForTest();
		Game joinedGame = gameService.joinGame(login3.getUserId(), game1.getId());
		
		try {
			gameService.joinGame(login3.getUserId(), game2.getId());
			fail();
		} catch (DataIntegrityViolationException e) {
			
		}
	}
	
	@Test
	public void assertGetOpenGames() throws Exception {
		Login login1 = createUserForTest();
		OpenGames openGamesBefore = gameService.getOpenGames(0);
		int sizeBefore = openGamesBefore.getGames().size();
		Game game1 = gameService.createGame(login1.getSessionId());
		OpenGames openGamesAfter = gameService.getOpenGames(0);
		assertEquals(openGamesAfter.getGames().size(), openGamesBefore.getGames().size() + 1);
	}
	
	private Login createUserForTest() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		return login;
	}
	
}
