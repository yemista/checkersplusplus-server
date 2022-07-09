package repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.checkersplusplus.dao.GameRepository;
import com.checkersplusplus.dao.models.GameModel;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.User;

import config.TestJpaConfig;
import util.UserNameTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
  classes = { TestJpaConfig.class }, 
  loader = AnnotationConfigContextLoader.class)
public class GameRepositoryTests {
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Test
	public void assertGetOpenGames() {
		//TODO fill this out
		fail();
	}
	
	@Test
	public void assertGetActiveGames() throws Exception {
		List<Game> activeGames = gameService.getActiveGames();
		Game game1 = createGame();
		Game game2 = createGame();
		List<Game> activeGamesAfterCreation = gameService.getActiveGames();
		assertEquals(activeGamesAfterCreation.size(), activeGames.size() + 2);
		assertTrue(activeGamesAfterCreation.stream().anyMatch(g -> game1.getId().equals(g.getId())));
		assertTrue(activeGamesAfterCreation.stream().anyMatch(g -> game2.getId().equals(g.getId())));
	}
	
	@Test
	public void assertGetActiveGameByToken() throws Exception {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		Game game = gameService.createGame(login.getSessionId());
		GameModel gameModel = gameRepository.getActiveGameByToken(login.getSessionId());
		assertNotNull(gameModel);
		assertEquals(game.getId(), gameModel.getId());
	}
	
	@Test
	public void assertForfeitGame() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		Game game = gameService.createGame(login.getSessionId());
		GameModel beforeForfeit = gameRepository.getById(game.getId());
		assertNull(beforeForfeit.getForfeitId());
		gameRepository.forfeitGame(game.getId(), login.getUserId());
		GameModel afterForfeit = gameRepository.getById(game.getId());
		assertNotNull(afterForfeit.getForfeitId());
		assertEquals(afterForfeit.getForfeitId(), login.getUserId());
	}
	
	private Game createGame() throws Exception {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		return gameService.createGame(login.getSessionId());
	}
}
