package com.checklersplusplus.server.service.job;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameMoveRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.LastMoveSentRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.internal.OpenWebSocket;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.GameMoveModel;
import com.checklersplusplus.server.model.LastMoveSentModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.websocket.WebSocketMap;
import com.checklersplusplus.server.websocket.WebSocketServerId;

@Profile("websocket")
@Service
public class SchedulerService {
	
	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

	private static final long THREE_SECOND_MILLIS = 3000;
	
	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private GameMoveRepository gameMoveRepository;
	
	@Autowired
	private LastMoveSentRepository lastMoveSentRepository;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	@Scheduled(fixedDelay = THREE_SECOND_MILLIS)
	public void updateClients() {
		try {
			List<OpenWebSocket> openWebSockets = getActiveOpenWebSockets();
			
			for (OpenWebSocket openWebSocket : openWebSockets) {
				Optional<SessionModel> serverSession = sessionRepository.getActiveBySessionId(openWebSocket.getSessionId());
				
				if (serverSession.isEmpty()) {
					continue;
				}
				
				UUID accountId = serverSession.get().getAccountId();
				List<GameEventModel> gameEvent = gameEventRepository.findByEventRecipientAccountIdAndActiveOrderByCreatedAsc(accountId, true);
								
				if (gameEvent.size() > 0) {
					for (GameEventModel gve : gameEvent) {
						forwardGameEvent(openWebSocket, gve.getGameEventId());
					}
					
					continue;
				}
				
				Optional<GameModel> game = gameRepository.getActiveGameByAccountId(accountId);
				
				if (game.isPresent()) {
					UUID gameId = game.get().getGameId();				
					Optional<LastMoveSentModel> lastMoveSent = lastMoveSentRepository.findFirstByAccountIdAndGameIdOrderByLastMoveSentDesc(accountId, gameId);
					
					if (game.get().getCurrentMoveNumber() == 0) {
						continue;
					}
					
					if (lastMoveSent.isPresent() && lastMoveSent.get().getLastMoveSent() == game.get().getCurrentMoveNumber()) {
						continue;
					}
					
					if (lastMoveSent.isEmpty() || game.get().getCurrentMoveNumber() - lastMoveSent.get().getLastMoveSent() == 1) {
						createLatestMoveEvent(gameId, accountId);
					} else {
						logger.error(String.format("Unexpected situation. Account id %s is more than one move behind", accountId.toString()));
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception thrown in scheduler service body", e);
		}
	}
	
	private List<OpenWebSocket> getActiveOpenWebSockets() {
		List<OpenWebSocketModel> openWebSocketModels = openWebSocketRepository.getActiveByServerId(WebSocketServerId.getInstance().getId());
		return openWebSocketModels.stream()
								  .map(model -> new OpenWebSocket(model.getSessionId(), model.getWebSocketId()))
								  .collect(Collectors.toList());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createLatestMoveEvent(UUID gameId, UUID accountId) {
		Optional<LastMoveSentModel> lastMoveSent = lastMoveSentRepository.findFirstByAccountIdAndGameIdOrderByLastMoveSentDesc(accountId, gameId);
		Optional<GameModel> game = gameRepository.findById(gameId);
		
		if (lastMoveSent.isPresent() && lastMoveSent.get().getLastMoveSent() == game.get().getCurrentMoveNumber()) {
			return;
		}
		
		Optional<GameMoveModel> latestMove = gameMoveRepository.findFirstByGameIdOrderByMoveNumberDesc(game.get().getGameId());
		String move = "MOVE|" + latestMove.get().getMoveNumber() + "|" + latestMove.get().getMoveList();
		logger.debug("Attempting to forward move: " + move);
		
		LastMoveSentModel latestMoveSent = new LastMoveSentModel();
		latestMoveSent.setGameId(gameId);
		latestMoveSent.setAccountId(accountId);
		latestMoveSent.setCreated(LocalDateTime.now());
		latestMoveSent.setLastMoveSent(game.get().getCurrentMoveNumber());
		lastMoveSentRepository.saveAndFlush(latestMoveSent);
		
		GameEventModel gameEvent = new GameEventModel();
		gameEvent.setActive(true);
		gameEvent.setCreated(LocalDateTime.now());
		gameEvent.setEvent(move);
		gameEvent.setEventRecipientAccountId(accountId);
		gameEvent.setGameId(gameId);
		gameEvent.setCreated(LocalDateTime.now());
		gameEventRepository.save(gameEvent);
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void forwardGameEvent(OpenWebSocket openWebSocket, UUID gameEventId) {
		Optional<GameEventModel> gameEvent = gameEventRepository.findById(gameEventId);
		
		logger.debug(String.format("Forwarding game event: %s to %s", gameEvent.get().getEvent(), gameEvent.get().getEventRecipientAccountId().toString()));
		
		if (gameEvent.isEmpty()) {
			logger.error("Missing gameEventId: %s", gameEventId.toString());
			return;
		}
		
		if (!gameEvent.get().isActive()) {
			logger.debug("Already forwarded gameEventId: %s", gameEventId.toString());
			return;
		}
		
		Optional<OpenWebSocketModel> socketModel = openWebSocketRepository.findByWebSocketId(openWebSocket.getWebSocketSessionId());
		
		if (socketModel.isEmpty() || socketModel.get().isActive() == false) {
			logger.debug("forwardGameEvent: Unexpected websocket disconnect for accountId %s gameId: %s", 
					gameEvent.get().getEventRecipientAccountId().toString(), gameEvent.get().getGameId().toString());
			return;
		}
		
		Pair<WebSocketSession, UUID> webSocket = WebSocketMap.getInstance().getMap().get(openWebSocket.getWebSocketSessionId());
		
		if (webSocket == null) {
			logger.debug("forwardGameEvent: Unexpected websocket close for accountId %s gameId: %s", 
					gameEvent.get().getEventRecipientAccountId().toString(), gameEvent.get().getGameId().toString());
			return;
		}
		
		try {
			webSocket.getFirst().sendMessage(new TextMessage(gameEvent.get().getEvent() + "|" + gameEvent.get().getGameEventId()));
		} catch (Exception e) {
			logger.error(String.format("Failed to send event %s to accountId %s for gameId %s", 
					gameEvent.get().getEvent(), gameEvent.get().getEventRecipientAccountId().toString(), gameEvent.get().getGameId().toString()), e);
		}
	}
}
