package com.checklersplusplus.server.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.request.CreateGame;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.SessionModel;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class PlayGameIntegrationTest {
	
	private static final String TEST_USERNAME_1 = "test1";
	private static final String TEST_EMAIL_1 = "test1@test.com";
	private static final String TEST_USERNAME_2 = "test2";
	private static final String TEST_EMAIL_2 = "test2@test.com";
	private static final String TEST_PASSWORD = "Testing123";
	
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
		UUID session1 = createAccountAndLogin(TEST_USERNAME_1, TEST_EMAIL_1);
		UUID session2 = createAccountAndLogin(TEST_USERNAME_2, TEST_EMAIL_2);
		UUID game = createGame(session1);
		List<Move> moves1 = new ArrayList<>();
		moves1.add(MOVE_1_2);
		moves1.add(MOVE_2_2);
		moves1.add(MOVE_3_2);
		TestWebSocketHandler webSocketHandler1 = createWebSocket(session1, moves1, 2);
		joinGame(session2, game);
		List<Move> moves2 = new ArrayList<>();
		moves2.add(MOVE_1_1);
		moves2.add(MOVE_2_1);
		moves2.add(MOVE_3_1);
		TestWebSocketHandler webSocketHandler2 = createWebSocket(session2, moves2, 1);
		
		// Make three moves from each side
		System.out.println("move 1");
		Thread.sleep(2000);
		makeMove(session1, game, MOVE_1_1);
		Thread.sleep(2000);
		makeMove(session2, game, MOVE_1_2);
		Thread.sleep(2000);
		
		System.out.println("move 2");
		makeMove(session1,game,  MOVE_2_1);
		Thread.sleep(2000);
		makeMove(session2, game, MOVE_2_2);
		Thread.sleep(2000);
		
		System.out.println("move 3");
		makeMove(session1, game, MOVE_3_1);
		Thread.sleep(2000);
		makeMove(session2, game, MOVE_3_2);
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

	private void makeMove(UUID session, UUID game, Move move) {
		List<Move> moves = new ArrayList<>();
		moves.add(move);
		ResponseEntity<Game> moveResponse = 
				restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/game/" + session + "/" + game.toString() + "/move", 
						moves,
						Game.class);
		assertEquals(moveResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(moveResponse.getBody().getMessage(), "Move successful.");
	}

	private TestWebSocketHandler createWebSocket(UUID session, List<Move> moves, int startingMoveNumber) {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketSession webSocketSession = null;
		TestWebSocketHandler testHandler = new TestWebSocketHandler(moves, startingMoveNumber);
		
		try {
			webSocketSession = webSocketClient.execute(testHandler, new WebSocketHttpHeaders(), URI.create(getWebSocketUrl())).get();
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
		
		return testHandler;
	}

	private void joinGame(UUID session, UUID game) {
		ResponseEntity<Game> joinGameResponse = 
				restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/game/" + session + "/" + game.toString() + "/join", 
						null,
						Game.class);
		assertEquals(joinGameResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(joinGameResponse.getBody().getMessage(), "Game joined.");
	}

	private UUID createGame(UUID session) {
		CreateGame createGame = new CreateGame();
		createGame.setMoveFirst(true);
		ResponseEntity<Game> createGameResponse = 
				restTemplate.postForEntity("http://localhost:" + String.valueOf(port) + "/checkersplusplus/api/game/" + session + "/create", createGame, Game.class);
		assertEquals(createGameResponse.getStatusCode(), HttpStatusCode.valueOf(200));
		assertEquals(createGameResponse.getBody().getMessage(), "Game created.");
		UUID gameId = createGameResponse.getBody().getGameId();
		Optional<GameModel> game = gameRepository.findById(gameId);
		gamesToDelete.add(game.get());
		return gameId;
	}

	private UUID createAccountAndLogin(String username, String email) {
		AccountModel account = new AccountModel();
		account.setCreated(LocalDateTime.now());
		account.setEmail(email);
		account.setPassword(TEST_PASSWORD);
		account.setUsername(username);
		account.setVerified(LocalDateTime.now());
		accountRepository.save(account);
		accountsToDelete.add(account);
		SessionModel session = new SessionModel();
		session.setActive(true);
		session.setLastModified(LocalDateTime.now());
		session.setAccountId(account.getAccountId());
		sessionRepository.save(session);
		sessionsToDelete.add(session);
		return session.getSessionId();
	}
	
	private String getWebSocketUrl() {
		return "ws://localhost:" + port + "/checkersplusplus/api/updates";
	}
	
	private class TestWebSocketHandler extends TextWebSocketHandler {
		private List<Move> moves;
		private int moveNumber;
		private List<Integer> movesReceived = new ArrayList<>();
		private int numErrors = 0;
		private List<String> errorMessages = new ArrayList<>();
		
		public TestWebSocketHandler(List<Move> moves, int moveNumber) {
			this.moves = moves;
			this.moveNumber = moveNumber;
		}
		
	    @Override
	    public void handleTextMessage(WebSocketSession session, TextMessage message) {
	       	String payload = message.getPayload();
	    	String[] parts = payload.split("\\|");
	    	
	    	if (parts.length != 3) {
	    		numErrors++;
	    		errorMessages.add("Invalid MOVE event from server: " + payload);
	    		return;
	    	}
	    	
	    	if (!"MOVE".equals(parts[0])) {
	    		numErrors++;
	    		errorMessages.add("Invalid event from server: " + payload);
	    		return;
	    	}
	    	
	    	int receivedMoveNumber = 0;
	    	
	    	try {
	    		receivedMoveNumber = Integer.parseInt(parts[1]);
	    	} catch (Exception e) {
	    		numErrors++;
	    		errorMessages.add("Invalid move number from server: " + payload);
	    		return;
	    	}
	    	
	    	// We are OK here. Duplicate move received which we will ignore
	    	if (movesReceived.contains(receivedMoveNumber)) {
	    		return;
	    	}
	    	
	    	if (receivedMoveNumber != moveNumber) {
	    		numErrors++;
	    		errorMessages.add("Mismatched move number from server. Got: " + receivedMoveNumber + " Expected: " + moveNumber);
	    		return;
	    	}
	    	
	    	movesReceived.add(receivedMoveNumber);
	    	moveNumber += 2;
	    	
	    	String move = parts[2];
	    	
	    	if (!moves.get(movesReceived.size() - 1).toString().equals(move)) {
	    		numErrors++;
	    		errorMessages.add("Mismatched move from server. Got: " + move + " Expected: " + moves.get(movesReceived.size() - 1).toString());
	    		return;
	    	}
	    }
	
	    @Override
	    public void afterConnectionEstablished(WebSocketSession session) {

	    }
	    
	    @Override
		public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
	    	session.close();
		}
	
		@Override
		public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
			System.out.println("Connection close");
			
			if (movesReceived.size() != moves.size()) {
	    		numErrors++;
	    		errorMessages.add("Did not receive all moves from server");
	    		System.out.println("Error after close: Did not receive all moves from server");
	    	}
			
			session.close();
		}
		
		public int getNumErrors() {
			return numErrors;
		}
		
		public List<String> getErrorMessages() {
			return errorMessages;
		}
	}
}
