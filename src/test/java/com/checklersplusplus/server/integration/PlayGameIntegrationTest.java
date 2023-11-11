package com.checklersplusplus.server.integration;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
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
import com.checklersplusplus.server.integration.util.IntegrationTestUtil;
import com.checklersplusplus.server.integration.util.TestWebSocketHandler;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.RatingModel;
import com.checklersplusplus.server.model.SessionModel;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class PlayGameIntegrationTest {
	
	private static final String TEST_USERNAME_1 = "test1";
	private static final String TEST_EMAIL_1 = "test1@test.com";
	private static final String TEST_USERNAME_2 = "test2";
	private static final String TEST_EMAIL_2 = "test2@test.com";
	
	private static final Move MOVE_1_1 = new Move(0, 2, 1, 3);
	private static final Move MOVE_2_1 = new Move(2, 2, 3, 3);
	private static final Move MOVE_3_1 = new Move(4, 2, 5, 3);
	
	private static final Move MOVE_1_2 = new Move(1, 5, 0, 4);
	private static final Move MOVE_2_2 = new Move(3, 5, 2, 4);
	private static final Move MOVE_3_2 = new Move(5, 5, 4, 4);

	@Autowired
	private TestRestTemplate restTemplate;
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<SessionModel> sessionsToDelete = new ArrayList<>();
	private List<GameModel> gamesToDelete = new ArrayList<>();
	private List<RatingModel> ratingsToDelete = new ArrayList<>();
	private List<OpenWebSocketModel> webSocketsToDelete = new ArrayList<>();
	
	@After
	public void cleanupDatabaseObjects() {
		accountsToDelete.forEach(account -> accountRepository.delete(account));
		sessionsToDelete.forEach(session -> sessionRepository.delete(session));
		gamesToDelete.forEach(game -> gameRepository.delete(game));
		webSocketsToDelete.forEach(webSocket -> openWebSocketRepository.delete(webSocket));
		ratingsToDelete.forEach(rating -> ratingRepository.delete(rating));
	}
	
	@Test
	public void playGame() throws InterruptedException {
		UUID session1 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_1, TEST_EMAIL_1);
		UUID session2 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_2, TEST_EMAIL_2);
		UUID game = IntegrationTestUtil.createGame(restTemplate, port, gameRepository, gamesToDelete, session1);
		List<Move> moves1 = new ArrayList<>();
		moves1.add(MOVE_1_2);
		moves1.add(MOVE_2_2);
		moves1.add(MOVE_3_2);
		TestWebSocketHandler webSocketHandler1 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session1, moves1, 2);
		IntegrationTestUtil.joinGame(restTemplate, port, session2, game);
		List<Move> moves2 = new ArrayList<>();
		moves2.add(MOVE_1_1);
		moves2.add(MOVE_2_1);
		moves2.add(MOVE_3_1);
		TestWebSocketHandler webSocketHandler2 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session2, moves2, 1);
		
		// Make three moves from each side
		System.out.println("move 1");
		Thread.sleep(2000);
		IntegrationTestUtil.makeMove(restTemplate, port, session1, game, MOVE_1_1);
		Thread.sleep(2000);
		IntegrationTestUtil.makeMove(restTemplate, port, session2, game, MOVE_1_2);
		Thread.sleep(2000);
		
		System.out.println("move 2");
		IntegrationTestUtil.makeMove(restTemplate, port, session1,game,  MOVE_2_1);
		Thread.sleep(2000);
		IntegrationTestUtil.makeMove(restTemplate, port, session2, game, MOVE_2_2);
		Thread.sleep(2000);
		
		System.out.println("move 3");
		IntegrationTestUtil.makeMove(restTemplate, port, session1, game, MOVE_3_1);
		Thread.sleep(2000);
		IntegrationTestUtil.makeMove(restTemplate, port, session2, game, MOVE_3_2);
		Thread.sleep(5000);
		
		System.out.println("Check for errors after close");
		
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
