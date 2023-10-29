package com.checklersplusplus.server.integration;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
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
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
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
	private OpenWebSocketRepository openWebSocketRepository;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<SessionModel> sessionsToDelete = new ArrayList<>();
	private List<GameModel> gamesToDelete = new ArrayList<>();
	private List<OpenWebSocketModel> webSocketsToDelete = new ArrayList<>();
	
	@After
	public void cleanupDatabaseObjects() {
		accountsToDelete.forEach(account -> accountRepository.delete(account));
		sessionsToDelete.forEach(session -> sessionRepository.delete(session));
		gamesToDelete.forEach(game -> gameRepository.delete(game));
		webSocketsToDelete.forEach(webSocket -> openWebSocketRepository.delete(webSocket));
	}
	
	@Test
	public void playGame() throws InterruptedException {
		UUID session1 = IntegrationTestUtil.createAccountAndLogin(accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, TEST_USERNAME_1, TEST_EMAIL_1);
		UUID session2 = IntegrationTestUtil.createAccountAndLogin(accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, TEST_USERNAME_2, TEST_EMAIL_2);
		UUID game = IntegrationTestUtil.createGame(restTemplate, port, gameRepository, gamesToDelete, session1);
		
		TestWebSocketHandler webSocketHandler1 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session2, Collections.emptyList(), 2);
		IntegrationTestUtil.joinGame(restTemplate, port, session2, game);
		TestWebSocketHandler webSocketHandler2 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session1, moves, 1);

		// TODO test by setting game state where one move will equal a win
		
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
