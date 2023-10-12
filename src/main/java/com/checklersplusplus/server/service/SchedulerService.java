package com.checklersplusplus.server.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.LastMoveSentRepository;
import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.LastMoveSentModel;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.websocket.WebSocketMap;
import com.checklersplusplus.server.websocket.WebSocketServerId;

@Service
@Transactional
public class SchedulerService {
	
	private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
	
	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private LastMoveSentRepository lastMoveSentRepository;
	
	@Scheduled(fixedDelay = 1000)
	public void updateClients() throws IOException {
		List<OpenWebSocketModel> openWebSockets = openWebSocketRepository.getActiveByServerId(WebSocketServerId.getInstance().getId());
		
		for (OpenWebSocketModel openWebSocket : openWebSockets) {
			Optional<SessionModel> serverSession = sessionRepository.getActiveBySessionId(openWebSocket.getSessionId());
			
			if (serverSession.isEmpty()) {
				continue;
			}
			
			UUID accountId = serverSession.get().getAccountId();
			Optional<GameModel> game = gameRepository.getActiveGameByAccountId(accountId);
			
			if (game.isEmpty()) {
				continue;
			}
			
			Optional<LastMoveSentModel> lastMoveSent = lastMoveSentRepository.findFirstByAccountIdAndGameIdOrderByLastMoveSentDesc(accountId, game.get().getGameId());
			
			if (lastMoveSent.get().getLastMoveSent() == game.get().getCurrentMoveNumber()) {
				continue;
			}
			
			if (game.get().getCurrentMoveNumber() - lastMoveSent.get().getLastMoveSent() == 1) {
				Pair<WebSocketSession, UUID> webSocket = WebSocketMap.getInstance().getMap().get(openWebSocket.getWebSocketId());
				String move = "";
				
				try {
					webSocket.getFirst().sendMessage(new TextMessage(move));
					LastMoveSentModel latestMoveSent = new LastMoveSentModel();
					latestMoveSent.setGameId(game.get().getGameId());
					latestMoveSent.setAccountId(accountId);
					latestMoveSent.setLastMoveSent(game.get().getCurrentMoveNumber());
					lastMoveSentRepository.save(latestMoveSent);
				} catch (Exception e) {
					logger.error(e.getMessage());
				}
			} else {
				// TODO error situation
			}
		}
	}
}
