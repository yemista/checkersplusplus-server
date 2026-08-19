package com.checklersplusplus.server.service.job;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameMoveRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.enums.GameEvent;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.GameMoveModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.service.RatingService;

/**
 * Ends games whose player on the clock has stopped playing.
 *
 * There are two deadlines:
 *
 *  1. The move deadline ({@code checkersplusplus.timeout.move.minutes}). A player who is
 *     connected but simply not moving loses once the last move is this old.
 *
 *  2. The disconnect deadline ({@code checkersplusplus.timeout.disconnect.seconds}), a
 *     much shorter grace period that applies only when the player on the clock is not
 *     connected. Presence comes from the websocket: an account is connected when it has
 *     an active session that still holds an open websocket, and the absence is timed
 *     from session.lastModified, which the websocket handler bumps on every heartbeat.
 *     That gives a last-seen timestamp without needing a new column.
 *
 * Set the disconnect deadline to 0 to switch the second rule off entirely.
 */
@Profile("websocket")
@Service
@Transactional
public class TimeoutService {

	private static final int ONE_SECONDS_MILLIS = 1 * 1000;
	private static final int ONE_MINUTE_MILLIS = 60 * 1000;

	private static final Logger logger = LoggerFactory.getLogger(TimeoutService.class);

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private SessionRepository sessionRepository;

	@Autowired
	private GameRepository gameRepository;

	@Autowired
	private GameMoveRepository gameMoveRepository;

	@Autowired
	private GameEventRepository gameEventRepository;

	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;

	@Autowired
	private RatingService ratingService;

	@Value("${checkersplusplus.timeout.minutes}")
	private Integer timeoutMinutes;

	@Value("${checkersplusplus.timeout.move.minutes}")
	private Integer moveTimeoutMinutes;

	@Value("${checkersplusplus.timeout.disconnect.seconds:30}")
	private Integer disconnectTimeoutSeconds;

	/**
	 * Retires sessions that have stopped heartbeating. Purely housekeeping: losing a game
	 * by disconnect is decided in {@link #checkForMoveTimeouts()}, which looks at the
	 * websocket directly rather than waiting for this to run.
	 */
	@Scheduled(fixedDelay = ONE_MINUTE_MILLIS)
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	public void checkForSessionTimeouts() {
		try {
			LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(timeoutMinutes);
			List<SessionModel> expiredSessions =
					sessionRepository.findByActiveAndLastModifiedLessThan(true, timeoutThreshold);
			List<UUID> staleSessionIds = new ArrayList<>();

			for (SessionModel session : expiredSessions) {
				// Leave a player who is still in a running game alone. The move and
				// disconnect deadlines finish that game first, and this sweeper picks the
				// session up on a later pass.
				if (gameRepository.getActiveGameByAccountId(session.getAccountId()).isPresent()) {
					continue;
				}

				staleSessionIds.add(session.getSessionId());
			}

			if (!staleSessionIds.isEmpty()) {
				// Invalidate exactly these rows. Deactivating by account id would also
				// kill a newer session the same account had just created by logging back
				// in while the old row was still stale.
				sessionRepository.invalidateSessionsBySessionIds(staleSessionIds);
				logger.info(String.format("Expired %d stale session(s)", staleSessionIds.size()));
			}
		} catch (Exception e) {
			logger.error("Exception thrown in session timeout sweeper", e);
		}
	}

	@Scheduled(fixedDelay = ONE_SECONDS_MILLIS)
	@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
	public void checkForMoveTimeouts() {
		try {
			List<GameModel> activeGames = gameRepository.getRunningGames();

			for (GameModel game : activeGames) {
				if (!game.isInProgress()) {
					continue;
				}

				UUID blackId = game.getBlackId();
				UUID redId = game.getRedId();

				if (blackId == null || redId == null) {
					continue;
				}

				Optional<GameMoveModel> latestMove =
						gameMoveRepository.findFirstByGameIdOrderByMoveNumberDesc(game.getGameId());

				UUID nextToMove;
				LocalDateTime onTheClockSince;

				if (latestMove.isPresent()) {
					nextToMove = latestMove.get().getMoveNumber() % 2 == 1 ? redId : blackId;
					onTheClockSince = latestMove.get().getCreated();
				} else {
					// No move has been made yet, so black is on the clock from the moment
					// the game started.
					nextToMove = blackId;
					onTheClockSince = game.getLastModified();
				}

				if (onTheClockSince == null || accountRepository.findById(nextToMove).isEmpty()) {
					continue;
				}

				UUID opponent = nextToMove.equals(blackId) ? redId : blackId;

				// Handled per game so that one bad row cannot stall the whole sweep,
				// which runs every second.
				try {
					if (tooOld(onTheClockSince, Duration.ofMinutes(moveTimeoutMinutes))) {
						endGameByTimeout(game, nextToMove, opponent, "ran out of time");
					} else if (disconnectedTooLong(nextToMove, onTheClockSince)) {
						endGameByTimeout(game, nextToMove, opponent, "lost connection");
					}
				} catch (Exception e) {
					logger.error(String.format("Failed to time out gameId %s", game.getGameId()), e);
				}
			}
		} catch (Exception e) {
			logger.error("Exception thrown in timeout service body", e);
		}
	}

	/**
	 * True when the player on the clock is not connected and has stayed that way past the
	 * disconnect grace period.
	 */
	private boolean disconnectedTooLong(UUID accountId, LocalDateTime onTheClockSince) {
		if (disconnectTimeoutSeconds == null || disconnectTimeoutSeconds <= 0) {
			return false;
		}

		Duration grace = Duration.ofSeconds(disconnectTimeoutSeconds);

		// Always allow the full grace period from the moment it became this player's
		// turn, so a brief drop right after the opponent moves does not forfeit instantly.
		if (!tooOld(onTheClockSince, grace)) {
			return false;
		}

		// Bots never hold a websocket, so they can only lose on the move deadline.
		if (isBot(accountId)) {
			return false;
		}

		Optional<SessionModel> session = sessionRepository.getActiveByAccountId(accountId);

		// No active session at all means the player is definitively gone.
		if (session.isEmpty()) {
			return true;
		}

		// The heartbeat is the primary signal. The websocket handler bumps
		// session.lastModified on every heartbeat, so it stops advancing the moment the
		// connection drops. It also covers the case where a websocket server dies without
		// closing its sockets, which leaves a stale open_web_socket row looking healthy.
		if (!tooOld(session.get().getLastModified(), grace)) {
			return false;
		}

		// Heartbeat has gone quiet. Confirm against the socket registry before taking the
		// game away, so a client that is still connected but briefly silent is not
		// punished - it falls through to the much longer move deadline instead.
		Optional<OpenWebSocketModel> socket =
				openWebSocketRepository.getActiveByServerSessionId(session.get().getSessionId());

		return socket.isEmpty();
	}

	private void endGameByTimeout(GameModel game, UUID loserId, UUID winnerId, String reason)
			throws CheckersPlusPlusServerException {
		game.setWinnerId(winnerId);
		game.setInProgress(false);
		game.setActive(false);
		game.setFinalized(true);
		game.setLastModified(LocalDateTime.now());
		gameRepository.save(game);

		Map<UUID, Integer> newRatings = ratingService.updatePlayerRatings(game.getGameId());

		saveEvent(game.getGameId(), winnerId, GameEvent.TIMEOUT.getMessage() + "|" + newRatings.get(winnerId));

		// Bots are deliberately never released here. They are a permanent pool, so their
		// in_use flag is left alone when a game times out.

		if (!isBot(loserId) && sessionRepository.getActiveByAccountId(loserId).isPresent()) {
			saveEvent(game.getGameId(), loserId,
					GameEvent.TIMEOUT_LOSS.getMessage() + "|" + newRatings.get(loserId));
		}

		logger.info(String.format("GameId: %s  Player %s %s. Winner: %s",
				game.getGameId(), loserId, reason, winnerId));
	}

	private void saveEvent(UUID gameId, UUID recipientAccountId, String event) {
		GameEventModel model = new GameEventModel();
		model.setActive(true);
		model.setCreated(LocalDateTime.now());
		model.setEvent(event);
		model.setEventRecipientAccountId(recipientAccountId);
		model.setGameId(gameId);
		gameEventRepository.save(model);
	}

	private boolean isBot(UUID accountId) {
		Optional<AccountModel> account = accountRepository.findById(accountId);
		return account.isPresent() && account.get().isBot();
	}

	private boolean tooOld(LocalDateTime timestamp, Duration limit) {
		if (timestamp == null) {
			return true;
		}

		return timestamp.isBefore(LocalDateTime.now().minus(limit));
	}
}
