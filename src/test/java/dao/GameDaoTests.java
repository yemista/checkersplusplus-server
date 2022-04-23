package dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkersplusplus.dao.GameDao;
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
public class GameDaoTests {

	@Autowired
	private GameDao gameDao;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	@Test
	public void assertGetActiveGames() {
		List<Game> activeGames = gameDao.getActiveGames();
		Game game1 = createGame();
		Game game2 = createGame();
		List<Game> activeGamesAfterCreation = gameDao.getActiveGames();
		assertEquals(activeGamesAfterCreation.size(), activeGames.size() + 2);
		assertTrue(activeGamesAfterCreation.stream().anyMatch(g -> game1.getId().equals(g.getId())));
		assertTrue(activeGamesAfterCreation.stream().anyMatch(g -> game2.getId().equals(g.getId())));
	}
	
	private Game createGame() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		return gameService.createGame(login.getSessionId());
	}
}
