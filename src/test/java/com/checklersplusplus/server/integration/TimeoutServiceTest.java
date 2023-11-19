package com.checklersplusplus.server.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
public class TimeoutServiceTest {

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
	public void timeoutTest() throws InterruptedException {
		UUID session1 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_1, TEST_EMAIL_1);
		UUID session2 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_2, TEST_EMAIL_2);
		TestWebSocketHandler webSocketHandler1 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session1, 
				Collections.emptyList());
		IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session2, 
				Collections.emptyList());
		
		Thread.sleep(45 * 1000);
		webSocketHandler1.sendMessage(session1.toString());
		Thread.sleep(30 * 1000);
		
		Optional<SessionModel> sessionModel1 = sessionRepository.findById(session1);
		assertThat(sessionModel1.isPresent()).isTrue();
		assertThat(sessionModel1.get().isActive()).isTrue();
		Optional<SessionModel> sessionModel2 = sessionRepository.findById(session2);
		assertThat(sessionModel2.isPresent()).isTrue();
		assertThat(sessionModel2.get().isActive()).isFalse();
	}
	
	@Test
	public void timeoutWhilePlayingGameTest() throws InterruptedException {
		UUID session1 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_1, TEST_EMAIL_1);
		UUID session2 = IntegrationTestUtil.createAccountAndLogin(ratingRepository, accountRepository, sessionRepository, accountsToDelete, sessionsToDelete, ratingsToDelete, TEST_USERNAME_2, TEST_EMAIL_2);
		UUID game = IntegrationTestUtil.createGame(restTemplate, port, gameRepository, gamesToDelete, session1);
		TestWebSocketHandler webSocketHandler1 = IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session1, 
				Arrays.asList(GameEvent.TIMEOUT.getMessage()));
		IntegrationTestUtil.joinGame(restTemplate, port, session2, game);
		IntegrationTestUtil.createWebSocket(restTemplate, port, openWebSocketRepository, webSocketsToDelete, session2, 
				Collections.emptyList());
		
		Thread.sleep(45 * 1000);
		webSocketHandler1.sendMessage(session1.toString());
		Thread.sleep(30 * 1000);
		
		Optional<SessionModel> sessionModel1 = sessionRepository.findById(session1);
		assertThat(sessionModel1.isPresent()).isTrue();
		assertThat(sessionModel1.get().isActive()).isTrue();
		Optional<SessionModel> sessionModel2 = sessionRepository.findById(session2);
		assertThat(sessionModel2.isPresent()).isTrue();
		assertThat(sessionModel2.get().isActive()).isFalse();
		Optional<GameModel> finishedGame = gameRepository.findById(game);
		assertThat(finishedGame.isPresent()).isTrue();
		assertThat(finishedGame.get().isInProgress()).isFalse();
		assertThat(finishedGame.get().isActive()).isFalse();
		assertThat(finishedGame.get().getWinnerId()).isEqualTo(sessionModel1.get().getAccountId());
	}
}
