package com.checklersplusplus.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameMoveRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.GameMoveModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.service.job.TimeoutService;

/**
 * Covers the disconnect deadline added to TimeoutService, plus the session sweeper.
 *
 * Pure Mockito: no Spring context and no datasource, so this never reaches a database.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TimeoutServiceDisconnectTest {

	private static final UUID BLACK = UUID.fromString("11111111-1111-1111-1111-111111111111");
	private static final UUID RED = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final UUID GAME = UUID.fromString("33333333-3333-3333-3333-333333333333");
	private static final UUID BLACK_SESSION = UUID.fromString("44444444-4444-4444-4444-444444444444");

	private static final int MOVE_TIMEOUT_MINUTES = 3;
	private static final int DISCONNECT_TIMEOUT_SECONDS = 30;

	@Mock private AccountRepository accountRepository;
	@Mock private SessionRepository sessionRepository;
	@Mock private GameRepository gameRepository;
	@Mock private GameMoveRepository gameMoveRepository;
	@Mock private GameEventRepository gameEventRepository;
	@Mock private OpenWebSocketRepository openWebSocketRepository;
	@Mock private RatingService ratingService;

	@InjectMocks private TimeoutService timeoutService;

	@BeforeEach
	void setUp() throws Exception {
		ReflectionTestUtils.setField(timeoutService, "timeoutMinutes", 15);
		ReflectionTestUtils.setField(timeoutService, "moveTimeoutMinutes", MOVE_TIMEOUT_MINUTES);
		ReflectionTestUtils.setField(timeoutService, "disconnectTimeoutSeconds", DISCONNECT_TIMEOUT_SECONDS);

		Map<UUID, Integer> ratings = new HashMap<>();
		ratings.put(BLACK, 780);
		ratings.put(RED, 820);
		when(ratingService.updatePlayerRatings(any())).thenReturn(ratings);

		// Black is on the clock in every scenario below; make it a human by default.
		account(BLACK, false);
		account(RED, false);
	}

	// ------------------------------------------------------------------ helpers

	private void account(UUID accountId, boolean bot) {
		AccountModel account = new AccountModel();
		account.setAccountId(accountId);
		account.setBot(bot);
		when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
	}

	/** A running game with one move already played, so black is next to move. */
	private GameModel runningGame(LocalDateTime lastMoveAt) {
		GameModel game = new GameModel();
		game.setGameId(GAME);
		game.setBlackId(BLACK);
		game.setRedId(RED);
		game.setActive(true);
		game.setInProgress(true);
		game.setGameState("EoEoEoEooEoEoEoEEoEoEoEoEEEEEEEEEEEEEEEExExExExEExExExExxExExExE|2");
		game.setCurrentMoveNumber(2);
		game.setLastModified(lastMoveAt);

		GameMoveModel move = new GameMoveModel();
		move.setGameId(GAME);
		// An even move number means black is next, matching Game.fromModel.
		move.setMoveNumber(2);
		move.setCreated(lastMoveAt);

		when(gameRepository.getRunningGames()).thenReturn(Collections.singletonList(game));
		when(gameMoveRepository.findFirstByGameIdOrderByMoveNumberDesc(GAME)).thenReturn(Optional.of(move));
		return game;
	}

	private void blackSession(LocalDateTime lastHeartbeat) {
		SessionModel session = new SessionModel();
		session.setSessionId(BLACK_SESSION);
		session.setAccountId(BLACK);
		session.setActive(true);
		session.setLastModified(lastHeartbeat);
		when(sessionRepository.getActiveByAccountId(BLACK)).thenReturn(Optional.of(session));
	}

	private void blackHasOpenSocket(boolean open) {
		OpenWebSocketModel socket = new OpenWebSocketModel();
		socket.setSessionId(BLACK_SESSION);
		socket.setActive(true);
		when(openWebSocketRepository.getActiveByServerSessionId(BLACK_SESSION))
				.thenReturn(open ? Optional.of(socket) : Optional.empty());
	}

	private GameModel captureSavedGame() {
		ArgumentCaptor<GameModel> captor = ArgumentCaptor.forClass(GameModel.class);
		verify(gameRepository).save(captor.capture());
		return captor.getValue();
	}

	private List<String> savedEvents() {
		ArgumentCaptor<GameEventModel> captor = ArgumentCaptor.forClass(GameEventModel.class);
		verify(gameEventRepository, times(2)).save(captor.capture());
		return captor.getAllValues().stream().map(GameEventModel::getEvent).toList();
	}

	// ------------------------------------------------------------------ tests

	@Test
	@DisplayName("connected player with a recent move is left alone")
	void connectedAndRecent() {
		runningGame(LocalDateTime.now().minusSeconds(5));
		blackSession(LocalDateTime.now());
		blackHasOpenSocket(true);

		timeoutService.checkForMoveTimeouts();

		verify(gameRepository, never()).save(any());
	}

	@Test
	@DisplayName("connected player still loses on the normal move deadline")
	void connectedButIdlePastMoveDeadline() {
		runningGame(LocalDateTime.now().minusMinutes(MOVE_TIMEOUT_MINUTES + 1));
		blackSession(LocalDateTime.now());
		blackHasOpenSocket(true);

		timeoutService.checkForMoveTimeouts();

		GameModel saved = captureSavedGame();
		assertThat(saved.getWinnerId()).isEqualTo(RED);
		assertThat(saved.isActive()).isFalse();
		assertThat(saved.isInProgress()).isFalse();
		assertThat(saved.isFinalized()).isTrue();
	}

	@Test
	@DisplayName("disconnected player loses well before the move deadline")
	void disconnectedPastGrace() {
		// Only 45s since the opponent's move, far short of the 3 minute move deadline.
		runningGame(LocalDateTime.now().minusSeconds(45));
		blackSession(LocalDateTime.now().minusSeconds(45));
		blackHasOpenSocket(false);

		timeoutService.checkForMoveTimeouts();

		GameModel saved = captureSavedGame();
		assertThat(saved.getWinnerId()).isEqualTo(RED);

		List<String> events = savedEvents();
		assertThat(events).anyMatch(e -> e.startsWith("TIMEOUT|820"));
		assertThat(events).anyMatch(e -> e.startsWith("TIMEOUT_LOSS|780"));
	}

	@Test
	@DisplayName("disconnected player keeps the full grace period from the start of their turn")
	void disconnectedWithinGrace() {
		// Disconnected long ago, but it only just became their turn.
		runningGame(LocalDateTime.now().minusSeconds(5));
		blackSession(LocalDateTime.now().minusMinutes(10));
		blackHasOpenSocket(false);

		timeoutService.checkForMoveTimeouts();

		verify(gameRepository, never()).save(any());
	}

	@Test
	@DisplayName("a stale heartbeat alone does not forfeit while the socket is still registered")
	void staleHeartbeatButSocketStillOpen() {
		runningGame(LocalDateTime.now().minusSeconds(45));
		blackSession(LocalDateTime.now().minusSeconds(45));
		blackHasOpenSocket(true);

		timeoutService.checkForMoveTimeouts();

		verify(gameRepository, never()).save(any());
	}

	@Test
	@DisplayName("no active session at all counts as disconnected")
	void noActiveSession() {
		runningGame(LocalDateTime.now().minusSeconds(45));
		when(sessionRepository.getActiveByAccountId(BLACK)).thenReturn(Optional.empty());

		timeoutService.checkForMoveTimeouts();

		assertThat(captureSavedGame().getWinnerId()).isEqualTo(RED);
	}

	@Test
	@DisplayName("bots are exempt from the disconnect deadline")
	void botsAreExempt() {
		account(BLACK, true);
		runningGame(LocalDateTime.now().minusSeconds(45));
		when(sessionRepository.getActiveByAccountId(BLACK)).thenReturn(Optional.empty());

		timeoutService.checkForMoveTimeouts();

		verify(gameRepository, never()).save(any());
	}

	@Test
	@DisplayName("setting the disconnect deadline to 0 switches the rule off")
	void disconnectDeadlineDisabled() {
		ReflectionTestUtils.setField(timeoutService, "disconnectTimeoutSeconds", 0);
		runningGame(LocalDateTime.now().minusSeconds(45));
		when(sessionRepository.getActiveByAccountId(BLACK)).thenReturn(Optional.empty());

		timeoutService.checkForMoveTimeouts();

		verify(gameRepository, never()).save(any());
	}

	@Test
	@DisplayName("a bot that loses on time is not released; the pool is permanent")
	void losingBotIsNotReleased() {
		account(BLACK, true);

		// Past the move deadline, which bots are still subject to.
		runningGame(LocalDateTime.now().minusMinutes(MOVE_TIMEOUT_MINUTES + 1));
		blackSession(LocalDateTime.now());
		blackHasOpenSocket(true);

		timeoutService.checkForMoveTimeouts();

		// The game still ends, but nothing touches the bot pool.
		assertThat(captureSavedGame().getWinnerId()).isEqualTo(RED);
	}

	@Test
	@DisplayName("session sweeper invalidates only the stale rows it found")
	void sweeperInvalidatesBySessionId() {
		UUID idleSessionId = UUID.randomUUID();
		UUID idleAccountId = UUID.randomUUID();

		SessionModel idle = new SessionModel();
		idle.setSessionId(idleSessionId);
		idle.setAccountId(idleAccountId);
		idle.setActive(true);
		idle.setLastModified(LocalDateTime.now().minusHours(1));

		when(sessionRepository.findByActiveAndLastModifiedLessThan(any(Boolean.class), any()))
				.thenReturn(Collections.singletonList(idle));
		when(gameRepository.getActiveGameByAccountId(idleAccountId)).thenReturn(Optional.empty());

		timeoutService.checkForSessionTimeouts();

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<UUID>> captor = ArgumentCaptor.forClass(List.class);
		verify(sessionRepository).invalidateSessionsBySessionIds(captor.capture());
		assertThat(captor.getValue()).containsExactly(idleSessionId);

		// The blunt by-account version would also kill a fresh login, so it must not run.
		verify(sessionRepository, never()).inactiveExistingSessions(any());
	}

	@Test
	@DisplayName("session sweeper leaves a player who is still in a game")
	void sweeperSkipsPlayersInGames() {
		UUID busyAccountId = UUID.randomUUID();

		SessionModel busy = new SessionModel();
		busy.setSessionId(UUID.randomUUID());
		busy.setAccountId(busyAccountId);
		busy.setActive(true);
		busy.setLastModified(LocalDateTime.now().minusHours(1));

		when(sessionRepository.findByActiveAndLastModifiedLessThan(any(Boolean.class), any()))
				.thenReturn(Arrays.asList(busy));
		when(gameRepository.getActiveGameByAccountId(busyAccountId))
				.thenReturn(Optional.of(new GameModel()));

		timeoutService.checkForSessionTimeouts();

		verify(sessionRepository, never()).invalidateSessionsBySessionIds(any());
	}
}
