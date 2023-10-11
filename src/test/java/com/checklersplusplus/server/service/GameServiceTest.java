package com.checklersplusplus.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.entities.response.Session;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.model.VerifyAccountModel;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameServiceTest {

	private static final String TEST_EMAIL = "test@test.com";
	private static final String TEST_PASSWORD = "Password123";
	private static final String TEST_USERNAME = "test";
	private static final String TEST_VERIFICATION_CODE = "ABCDEF";
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<VerifyAccountModel> verifyAccountsToDelete = new ArrayList<>();
	private List<GameModel> gamesToDelete = new ArrayList<>();
	private List<SessionModel> sessionsToDelete = new ArrayList<>();
	
	private UUID accountId;
	private UUID sessionId;
	
	@Before
	public void createAccountForTest() throws Exception {
		accountService.createAccount(new CreateAccount(TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD, TEST_USERNAME));
		Optional<AccountModel> account = accountRepository.getByUsername(TEST_USERNAME);
		assertThat(account.isPresent()).isTrue();
		accountId = account.get().getAccountId();
		account.get().setVerified(LocalDateTime.now());
		accountRepository.save(account.get());
		accountsToDelete.add(account.get());
		
		Optional<VerifyAccountModel> verifyAccountModel = verifyAccountRepository.getActiveByAccountId(accountId);
		assertThat(verifyAccountModel.isPresent()).isTrue();
		verifyAccountsToDelete.add(verifyAccountModel.get());
		
		Session session = accountService.login(TEST_USERNAME, TEST_PASSWORD);
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(session.getSessionId());
		assertThat(sessionModel.isPresent()).isTrue();
		sessionId = sessionModel.get().getSessionId();
		sessionsToDelete.add(sessionModel.get());
	}
	
	
	@After
	public void cleanupDatabaseObjects() {
		gamesToDelete.forEach(game -> gameRepository.delete(game));
		sessionsToDelete.forEach(verifyAccount -> sessionRepository.delete(verifyAccount));
		verifyAccountsToDelete.forEach(verifyAccount -> verifyAccountRepository.delete(verifyAccount));
		accountsToDelete.forEach(account -> accountRepository.delete(account));
	}
	
	@Test
	public void cannotCreateGameInvalidSession() {
		try {
			gameService.createGame(UUID.randomUUID(), false);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Session not found. Please login.");
		}
	}
	
	@Test
	public void cannotCreateGameUserHasActiveGame() {
		GameModel game = new GameModel();
		game.setActive(true);
		game.setInProgress(true);
		game.setBlackId(accountId);
		game.setCreated(LocalDateTime.now());
		game.setGameState("");
		game.setLastModified(LocalDateTime.now());
		gameRepository.save(game);
		assertThat(game.getGameId()).isNotNull();
		gamesToDelete.add(game);
		
		try {
			gameService.createGame(UUID.randomUUID(), false);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Session not found. Please login.");
		}
	}
	
	@Test
	public void canCreateGameAsBlack() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		assertThat(gameModel.isPresent()).isTrue();
		assertThat(gameModel.get().getBlackId()).isEqualTo(accountId);
		assertThat(gameModel.get().getCreated()).isNotNull();
		assertThat(gameModel.get().getLastModified()).isNotNull();
		assertThat(gameModel.get().getWinnerId()).isNull();
		assertThat(gameModel.get().getRedId()).isNull();
		assertThat(gameModel.get().isActive()).isTrue();
		assertThat(gameModel.get().isInProgress()).isFalse();
		assertThat(gameModel.get().getGameState()).isEqualTo("EOEOEOEOOEOEOEOEEOEOEOEOEEEEEEEEEEEEEEEEXEXEXEXEEXEXEXEXXEXEXEXE|1");
	}
	
	@Test
	public void canCreateGameAsRed() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, false);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		assertThat(gameModel.isPresent()).isTrue();
		assertThat(gameModel.get().getRedId()).isEqualTo(accountId);
		assertThat(gameModel.get().getCreated()).isNotNull();
		assertThat(gameModel.get().getLastModified()).isNotNull();
		assertThat(gameModel.get().getWinnerId()).isNull();
		assertThat(gameModel.get().getBlackId()).isNull();
		assertThat(gameModel.get().isActive()).isTrue();
		assertThat(gameModel.get().isInProgress()).isFalse();
		assertThat(gameModel.get().getGameState()).isEqualTo("EOEOEOEOOEOEOEOEEOEOEOEOEEEEEEEEEEEEEEEEXEXEXEXEEXEXEXEXXEXEXEXE|1");
	}
	
	@Test
	public void cannotMoveInvalidMove() throws Exception {
		UUID secondAccountId = setupSecondUserAndSession();
		List<SessionModel> secondSession = sessionRepository.getActiveByAccountId(secondAccountId);
		Game game = gameService.createGame(secondSession.get(0).getSessionId(), false);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameService.joinGame(sessionId, game.getGameId());
		List<Move> moves = Arrays.asList(new Move(0, 2, 1, 2));
		
		try {
			gameService.move(sessionId, gameModel.get().getGameId(), moves);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Invalid move.");
		}
	}
	
	@Test
	public void canMove() throws Exception {
		UUID secondAccountId = setupSecondUserAndSession();
		List<SessionModel> secondSession = sessionRepository.getActiveByAccountId(secondAccountId);
		Game game = gameService.createGame(secondSession.get(0).getSessionId(), false);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameService.joinGame(sessionId, game.getGameId());
		assertThat(gameModel.get().getCurrentMoveNumber()).isEqualTo(1);
		List<Move> moves = Arrays.asList(new Move(0, 2, 1, 3));
		gameService.move(sessionId, gameModel.get().getGameId(), moves);
		Optional<GameModel> gameAfterMove = gameRepository.getByGameId(game.getGameId());
		assertThat(gameAfterMove.get().getCurrentMoveNumber()).isEqualTo(2);
		assertThat(gameAfterMove.get().getGameState()).isEqualTo("EOEOEOEOOEOEOEOEEOEOEOEOEEEEEEEEEXEEEEEEEEXEXEXEEXEXEXEXXEXEXEXE|2");
	}
	
	@Test
	public void cannotMoveInactiveSession() {
		try {
			gameService.move( UUID.randomUUID(), UUID.randomUUID(), Arrays.asList(new Move(0, 2, 1, 3)));
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Session not found. Please login.");
		}
	}
	
	@Test
	public void cannotMoveInvalidGame() {
		try {
			gameService.move(sessionId, UUID.randomUUID(), Arrays.asList(new Move(0, 2, 1, 3)));
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotMoveInactiveGame() throws Exception {
		UUID secondAccountId = setupSecondUserAndSession();
		List<SessionModel> secondSession = sessionRepository.getActiveByAccountId(secondAccountId);
		Game game = gameService.createGame(secondSession.get(0).getSessionId(), false);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameService.joinGame(sessionId, game.getGameId());
		Optional<GameModel> joinedGame = gameRepository.getByGameId(game.getGameId());
		joinedGame.get().setActive(false);
		gameRepository.save(joinedGame.get());
		List<Move> moves = Arrays.asList(new Move(0, 2, 1, 3));
		
		try {
			gameService.move(sessionId, gameModel.get().getGameId(), moves);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotMoveGameNotInProgress() throws Exception {
		UUID secondAccountId = setupSecondUserAndSession();
		List<SessionModel> secondSession = sessionRepository.getActiveByAccountId(secondAccountId);
		Game game = gameService.createGame(secondSession.get(0).getSessionId(), false);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameService.joinGame(sessionId, game.getGameId());
		Optional<GameModel> joinedGame = gameRepository.getByGameId(game.getGameId());
		joinedGame.get().setInProgress(false);
		gameRepository.save(joinedGame.get());
		List<Move> moves = Arrays.asList(new Move(0, 2, 1, 3));
		
		try {
			gameService.move(sessionId, gameModel.get().getGameId(), moves);
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void canJoinGameAsRed() throws Exception {
		UUID secondAccountId = setupSecondUserAndSession();
		List<SessionModel> secondSession = sessionRepository.getActiveByAccountId(secondAccountId);
		Game game = gameService.createGame(secondSession.get(0).getSessionId(), true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameService.joinGame(sessionId, game.getGameId());
		Optional<GameModel> joinedGame = gameRepository.getByGameId(game.getGameId());
		assertThat(joinedGame.get().getRedId()).isEqualTo(accountId);
		assertThat(joinedGame.get().isInProgress()).isTrue();
	}
	
	@Test
	public void canJoinGameAsBlack() throws Exception {
		UUID secondAccountId = setupSecondUserAndSession();
		List<SessionModel> secondSession = sessionRepository.getActiveByAccountId(secondAccountId);
		Game game = gameService.createGame(secondSession.get(0).getSessionId(), false);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameService.joinGame(sessionId, game.getGameId());
		Optional<GameModel> joinedGame = gameRepository.getByGameId(game.getGameId());
		assertThat(joinedGame.get().getBlackId()).isEqualTo(accountId);
		assertThat(joinedGame.get().isInProgress()).isTrue();
	}
	
	@Test
	public void cannotJoinGameInvalidGame() {
		try {
			gameService.joinGame(sessionId, UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotJoinGameInactiveGame() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameModel.get().setActive(false);
		gameRepository.save(gameModel.get());
		
		try {
			gameService.joinGame(sessionId, UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotJoinGameInProgressGame() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameModel.get().setInProgress(true);
		gameRepository.save(gameModel.get());
		
		try {
			gameService.joinGame(sessionId, UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotJoinGameSessionNotFound() throws CheckersPlusPlusServerException {
		try {
			gameService.joinGame(UUID.randomUUID(), UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Session not found. Please login.");
		}
	}
	
	@Test
	public void cannotForfeitGameInvalidGame() {
		try {
			gameService.forfeitGame(sessionId, UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotForfeitGameInactiveGame() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameModel.get().setActive(false);
		gameRepository.save(gameModel.get());
		
		try {
			gameService.forfeitGame(sessionId, gameModel.get().getGameId());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotForfeitGameNotInProgress() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		
		try {
			gameService.forfeitGame(sessionId, gameModel.get().getGameId());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotForfeitGameSessionAccountMismatch() throws Exception {
		UUID secondAccountId = setupSecondUserAndSession();
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameModel.get().setBlackId(secondAccountId);
		gameModel.get().setInProgress(true);
		gameRepository.save(gameModel.get());
		
		try {
			gameService.forfeitGame(sessionId, gameModel.get().getGameId());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("User not found for game.");
		}
	}

	@Test
	public void cannotForfeitGameSessionNotFound() throws CheckersPlusPlusServerException {
		try {
			gameService.forfeitGame(UUID.randomUUID(), UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Session not found. Please login.");
		}
	}
	
	@Test
	public void canForfeitGame() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		assertThat(gameModel.isPresent()).isTrue();
		gamesToDelete.add(gameModel.get());
		gameModel.get().setInProgress(true);
		UUID opponentId = UUID.randomUUID();
		gameModel.get().setRedId(opponentId);;
		gameRepository.save(gameModel.get());
		gameService.forfeitGame(sessionId, gameModel.get().getGameId());
		Optional<GameModel> forfeitedGameModel = gameRepository.getByGameId(game.getGameId());
		assertThat(forfeitedGameModel.isPresent()).isTrue();
		assertThat(forfeitedGameModel.get().isActive()).isFalse();
		assertThat(forfeitedGameModel.get().isInProgress()).isFalse();
		assertThat(forfeitedGameModel.get().getWinnerId()).isEqualTo(opponentId);
	}
	
	@Test
	public void cannotCancelGameInvalidGame() {
		try {
			gameService.cancelGame(sessionId, UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotCancelGameInactiveGame() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameModel.get().setActive(false);
		gameRepository.save(gameModel.get());
		
		try {
			gameService.cancelGame(sessionId, gameModel.get().getGameId());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotCancelGameSessionNotFound() throws CheckersPlusPlusServerException {
		try {
			gameService.cancelGame(UUID.randomUUID(), UUID.randomUUID());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Session not found. Please login.");
		}
	}
	
	@Test
	public void cannotCancelGameUserNotPlaying() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameModel.get().setBlackId(UUID.randomUUID());
		gameRepository.save(gameModel.get());
		
		try {
			gameService.cancelGame(sessionId, gameModel.get().getGameId());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void cannotCancelGameInProgress() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		gamesToDelete.add(gameModel.get());
		gameModel.get().setInProgress(true);;
		gameRepository.save(gameModel.get());
		
		try {
			gameService.cancelGame(sessionId, gameModel.get().getGameId());
			fail();
		} catch(CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Cannot cancel game because it has started.");
		}
	}
	
	@Test
	public void canCancelGame() throws CheckersPlusPlusServerException {
		Game game = gameService.createGame(sessionId, true);
		Optional<GameModel> gameModel = gameRepository.getByGameId(game.getGameId());
		assertThat(gameModel.isPresent()).isTrue();
		gamesToDelete.add(gameModel.get());
		gameService.cancelGame(sessionId, gameModel.get().getGameId());
		Optional<GameModel> forfeitedGameModel = gameRepository.getByGameId(game.getGameId());
		assertThat(forfeitedGameModel.isPresent()).isTrue();
		assertThat(forfeitedGameModel.get().isActive()).isFalse();
		assertThat(forfeitedGameModel.get().isInProgress()).isFalse();
		assertThat(forfeitedGameModel.get().getWinnerId()).isNull();
	}
	
	private UUID setupSecondUserAndSession() throws Exception {
		String username = TEST_USERNAME + "M";
		accountService.createAccount(new CreateAccount(TEST_EMAIL + "M", TEST_PASSWORD, TEST_PASSWORD, username));
		Optional<AccountModel> account = accountRepository.getByUsername(username);
		assertThat(account.isPresent()).isTrue();
		UUID secondAccountId = account.get().getAccountId();
		account.get().setVerified(LocalDateTime.now());
		accountRepository.save(account.get());
		accountsToDelete.add(account.get());
		
		Optional<VerifyAccountModel> verifyAccountModel = verifyAccountRepository.getActiveByAccountId(secondAccountId);
		assertThat(verifyAccountModel.isPresent()).isTrue();
		verifyAccountsToDelete.add(verifyAccountModel.get());
		
		Session session = accountService.login(username, TEST_PASSWORD);
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(session.getSessionId());
		assertThat(sessionModel.isPresent()).isTrue();
		sessionsToDelete.add(sessionModel.get());
		return secondAccountId;
	}
}
