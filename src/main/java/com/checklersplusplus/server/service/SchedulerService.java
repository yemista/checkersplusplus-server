package com.checklersplusplus.server.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameMoveRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.LastMoveSentRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.internal.OpenWebSocket;
import com.checklersplusplus.server.enums.GameEvent;
import com.checklersplusplus.server.model.GameEventModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.GameMoveModel;
import com.checklersplusplus.server.model.LastMoveSentModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.websocket.WebSocketMap;
import com.checklersplusplus.server.websocket.WebSocketServerId;

@Service
public class SchedulerService {
	
	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
	
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
	
	@Scheduled(fixedDelay = 1000)
	public void updateClients() throws IOException {
		List<OpenWebSocket> openWebSockets = getActiveOpenWebSockets();
		
		for (OpenWebSocket openWebSocket : openWebSockets) {
			Optional<SessionModel> serverSession = sessionRepository.getActiveBySessionId(openWebSocket.getSessionId());
			
			if (serverSession.isEmpty()) {
				continue;
			}
			
			UUID accountId = serverSession.get().getAccountId();
			Optional<GameModel> game = gameRepository.getActiveGameByAccountId(accountId);
			
			if (game.isEmpty()) {
				continue;
			}
			
			Optional<GameEventModel> gameEvent = gameEventRepository.findActiveEventForAccountIdAndGameId(accountId, game.get().getGameId());
			
			if (gameEvent.isPresent() && GameEvent.TIMEOUT.getMessage().equals(gameEvent.get().getEvent())) {
				forwardGameEvent(openWebSocket, gameEvent.get(), accountId, game.get().getGameId());
				game.get().setActive(false);
				game.get().setWinnerId(accountId);
				gameRepository.save(game.get());
				continue;
			}
			
			if (gameEvent.isPresent()
					&& (GameEvent.FORFEIT.getMessage().equals(gameEvent.get().getEvent()) || 
						GameEvent.BEGIN.getMessage().equals(gameEvent.get().getEvent()))) {
				forwardGameEvent(openWebSocket, gameEvent.get(), accountId, game.get().getGameId());				
				continue;
			}
			
			Optional<LastMoveSentModel> lastMoveSent = lastMoveSentRepository.findFirstByAccountIdAndGameIdOrderByLastMoveSentDesc(accountId, game.get().getGameId());
			
			if (lastMoveSent.isEmpty() || lastMoveSent.get().getLastMoveSent() == game.get().getCurrentMoveNumber()) {
				if (gameEvent.isPresent() && GameEvent.LOSE.getMessage().equals(gameEvent.get().getEvent())) {
					forwardGameEvent(openWebSocket, gameEvent.get(), accountId, game.get().getGameId());
				}
				
				continue;
			}
			
			if (game.get().getCurrentMoveNumber() - lastMoveSent.get().getLastMoveSent() == 1) {
				forwardLatestMove(openWebSocket, game.get(), accountId);
			} else {
				logger.error(String.format("Unexpected situation. Account id %s is more than one move behind", accountId.toString()));
			}
		}
	}
	
	private List<OpenWebSocket> getActiveOpenWebSockets() {
		List<OpenWebSocketModel> openWebSocketModels = openWebSocketRepository.getActiveByServerId(WebSocketServerId.getInstance().getId());
		return openWebSocketModels.stream()
								  .map(model -> new OpenWebSocket(model.getSessionId(), model.getWebSocketId()))
								  .collect(Collectors.toList());
}

	private void forwardLatestMove(OpenWebSocket openWebSocket, GameModel game, UUID accountId) {
		Pair<WebSocketSession, UUID> webSocket = WebSocketMap.getInstance().getMap().get(openWebSocket.getWebSocketSessionId());
		
		if (webSocket == null) {
			return;
		}
		
		Optional<GameMoveModel> latestMove = gameMoveRepository.findFirstByGameIdOrderByMoveNumberDesc(game.getGameId());
		String move = "MOVE|" + latestMove.get().getMoveList();
		
		try {
			webSocket.getFirst().sendMessage(new TextMessage(move));
			LastMoveSentModel latestMoveSent = new LastMoveSentModel();
			latestMoveSent.setGameId(game.getGameId());
			latestMoveSent.setAccountId(accountId);
			latestMoveSent.setLastMoveSent(game.getCurrentMoveNumber());
			lastMoveSentRepository.save(latestMoveSent);
		} catch (Exception e) {
			logger.error(String.format("Failed to send move number %d to accountId %s for gameId %s", 
					game.getCurrentMoveNumber(), accountId.toString(), game.getGameId().toString()), e);
		}
	}

	private void forwardGameEvent(OpenWebSocket openWebSocket, GameEventModel gameEvent, UUID accountId, UUID gameId) {
		Pair<WebSocketSession, UUID> webSocket = WebSocketMap.getInstance().getMap().get(openWebSocket.getWebSocketSessionId());
		
		if (webSocket == null) {
			return;
		}
		
		try {
			webSocket.getFirst().sendMessage(new TextMessage(gameEvent.getEvent()));
			gameEvent.setActive(false);
			gameEventRepository.save(gameEvent);
		} catch (Exception e) {
			logger.error(String.format("Failed to send event %s to accountId %s for gameId %s", 
					gameEvent.getEvent(), accountId.toString(), gameId.toString()), e);
		}
	}
}
