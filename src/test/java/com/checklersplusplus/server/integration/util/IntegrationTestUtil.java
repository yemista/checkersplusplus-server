package com.checklersplusplus.server.integration.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.RatingRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.request.CreateGame;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.RatingModel;
import com.checklersplusplus.server.model.SessionModel;

public class IntegrationTestUtil {
	
	private static final String TEST_PASSWORD = "Testing123";
	
	public static TestWebSocketHandler createWebSocket(TestRestTemplate restTemplate, int port, OpenWebSocketRepository openWebSocketRepository, 
			List<OpenWebSocketModel> webSocketsToDelete, UUID session, List<Move> moves, int startingMoveNumber) {
		TestWebSocketHandler testHandler = new TestWebSocketHandler(moves, startingMoveNumber);
		setupWebSocket(testHandler, port, openWebSocketRepository, webSocketsToDelete, session);
		return testHandler;
	}
	
	public static TestWebSocketHandler createWebSocket(TestRestTemplate restTemplate, int port, OpenWebSocketRepository openWebSocketRepository, 
			List<OpenWebSocketModel> webSocketsToDelete, UUID session, List<String> gameEvent) {
		TestWebSocketHandler testHandler = new TestWebSocketHandler(gameEvent);
		setupWebSocket(testHandler, port, openWebSocketRepository, webSocketsToDelete, session);
		return testHandler;
	}
	
	private static void setupWebSocket(TestWebSocketHandler testHandler, int port, OpenWebSocketRepository openWebSocketRepository, 
			List<OpenWebSocketModel> webSocketsToDelete, UUID session) {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketSession webSocketSession = null;
		
		try {
			webSocketSession = webSocketClient.execute(testHandler, new WebSocketHttpHeaders(), URI.create(getWebSocketUrl(port))).get();
			webSocketSession.sendMessage(new TextMessage(session.toString()));
			assertThat(webSocketSession.isOpen()).isTrue();
			
			Thread.sleep(100);
			Optional<OpenWebSocketModel> openWebSocketModel = openWebSocketRepository.getActiveByServerSessionId(session);
			assertThat(openWebSocketModel.isPresent()).isTrue();
			webSocketsToDelete.add(openWebSocketModel.get());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getWebSocketUrl(int port) {
		return "ws://localhost:" + port + "/checkersplusplus/api/updates";
	}
	
	public static void joinGame(TestRestTemplate restTemplate, int port, UUID session, UUID game) {
		ResponseEntity<Game> joinGameResponse = 
				restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/game/" + session + "/" + game.toString() + "/join", 
						null,
						Game.class);
		assertEquals(joinGameResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(joinGameResponse.getBody().getMessage(), "Game joined.");
	}

	public static UUID createGame(TestRestTemplate restTemplate, int port, GameRepository gameRepository, List<GameModel> gamesToDelete, UUID session) {
		CreateGame createGame = new CreateGame();
		createGame.setMoveFirst(true);
		ResponseEntity<Game> createGameResponse = 
				restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/game/" + session + "/create", createGame, Game.class);
		assertEquals(createGameResponse.getBody().getMessage(), "Game created.");
		assertEquals(createGameResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		UUID gameId = createGameResponse.getBody().getGameId();
		Optional<GameModel> game = gameRepository.findById(gameId);
		gamesToDelete.add(game.get());
		return gameId;
	}

	public static UUID createAccountAndLogin(RatingRepository ratingRepository, AccountRepository accountRepository, SessionRepository sessionRepository, 
			List<AccountModel> accountsToDelete, List<SessionModel> sessionsToDelete, List<RatingModel> ratingsToDelete, String username, String email) {
		AccountModel account = new AccountModel();
		account.setCreated(LocalDateTime.now());
		account.setEmail(email);
		account.setPassword(TEST_PASSWORD);
		account.setUsername(username);
		account.setVerified(LocalDateTime.now());
		accountRepository.save(account);
		accountsToDelete.add(account);
		
		RatingModel rating = new RatingModel();
		rating.setAccountId(account.getAccountId());
		rating.setRating(800);
		ratingRepository.save(rating);
		ratingsToDelete.add(rating);
		
		SessionModel session = new SessionModel();
		session.setActive(true);
		session.setLastModified(LocalDateTime.now());
		session.setAccountId(account.getAccountId());
		sessionRepository.save(session);
		sessionsToDelete.add(session);
		return session.getSessionId();
	}
	
	public static void makeMove(TestRestTemplate restTemplate, int port, UUID session, UUID game, Move move) {
		List<Move> moves = new ArrayList<>();
		moves.add(move);
		ResponseEntity<Game> moveResponse = 
				restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/game/" + session + "/" + game.toString() + "/move", 
						moves,
						Game.class);
		assertEquals(moveResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(moveResponse.getBody().getMessage(), "Move successful.");
	}
}
