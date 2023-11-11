package com.checklersplusplus.server.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.RatingRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.enums.GameEvent;
import com.checklersplusplus.server.integration.util.IntegrationTestUtil;
import com.checklersplusplus.server.integration.util.TestWebSocketHandler;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.RatingModel;
import com.checklersplusplus.server.model.SessionModel;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class WinGameIntegrationTest {
	private static final String TEST_USERNAME_1 = "test1";
	private static final String TEST_EMAIL_1 = "test1@test.com";
	private static final String TEST_USERNAME_2 = "test2";
	private static final String TEST_EMAIL_2 = "test2@test.com";

	@Autowired
	private TestRestTemplate restTemplate;
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<SessionModel> sessionsToDelete = new ArrayList<>();
	private List<GameModel> gamesToDelete = new ArrayList<>();
	private List<OpenWebSocketModel> webSocketsToDelete = new ArrayList<>();
	private List<RatingModel> ratingsToDelete = new ArrayList<>();
	
	@After
	public void cleanupDatabaseObjects() {
		accountsToDelete.forEach(account -> accountRepository.delete(account));
		sessionsToDelete.forEach(session -> sessionRepository.delete(session));
		gamesToDelete.forEach(game -> gameRepository.delete(game));
		webSocketsToDelete.forEach(webSocket -> openWebSocketRepository.delete(webSocket));
		ratingsToDelete.forEach(rating -> ratingRepository.delete(rating));
	}
	
	@Test
	public void winGame() throws InterruptedException {
		UUID session1 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_1, TEST_EMAIL_1);
		UUID session2 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_2, TEST_EMAIL_2);
		UUID game = IntegrationTestUtil.createGame(restTemplate, port, gameRepository, gamesToDelete, session1);
		
		Move redMove = new Move(3, 3, 5, 1);
		List<String> winnerEvents = Arrays.asList(GameEvent.WIN.getMessage());
		List<String> loserEvents = Arrays.asList(GameEvent.LOSE.getMessage());
		
		TestWebSocketHandler webSocketHandler1 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session2, winnerEvents);
		IntegrationTestUtil.joinGame(restTemplate, port, session2, game);
		TestWebSocketHandler webSocketHandler2 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session1, loserEvents);

		String boardState = "EEEEEoEoEEEEEEoEEEEEEoEoEEEEEEEEEEEoEEEEEEEExEEEEEEEEEEEEEEEEEEE|1";
		Optional<GameModel> gameModel = gameRepository.getByGameId(game);
		gameModel.get().setGameState(boardState);
		gameModel.get().setCurrentMoveNumber(1);
		gameRepository.save(gameModel.get());
		
		IntegrationTestUtil.makeMove(restTemplate, port, session2, game, redMove);
		Thread.sleep(2000);
		
		Optional<SessionModel> winnerSession = sessionRepository.getActiveBySessionId(session2);
		UUID winnerId = winnerSession.get().getAccountId();
		
		Optional<GameModel> updatedGameModel = gameRepository.getByGameId(game);
		assertThat(updatedGameModel.get().getWinnerId()).isEqualTo(winnerId);
		assertThat(updatedGameModel.get().isActive()).isFalse();
		assertThat(updatedGameModel.get().isInProgress()).isFalse();
		
		System.out.println("Errors");
		
		if (webSocketHandler1.getNumErrors() > 0) {
			webSocketHandler1.getErrorMessages().forEach(m -> System.out.println(m));
			fail();
		}
		
		if (webSocketHandler2.getNumErrors() > 0) {
			webSocketHandler2.getErrorMessages().forEach(m -> System.out.println(m));
			fail();
		}
		
		System.out.println("Finish");
	}
	
}
