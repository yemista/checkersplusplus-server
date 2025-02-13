package com.checklersplusplus.server.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.entities.internal.OpenWebSocket;
import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.websocket.WebSocketServerId;

@Service
@Transactional
public class OpenWebSocketService {
	private static final Logger logger = LoggerFactory.getLogger(OpenWebSocketService.class);

	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;

	public UUID createWebSocketSession(UUID serverSessionId, String webSocketSessionId) {
		OpenWebSocketModel openWebSocket = new OpenWebSocketModel();
		openWebSocket.setActive(true);
		openWebSocket.setCreated(LocalDateTime.now());
		openWebSocket.setWebSocketId(webSocketSessionId);
		openWebSocket.setSessionId(serverSessionId);
		openWebSocket.setServerId(WebSocketServerId.getInstance().getId());
		openWebSocketRepository.save(openWebSocket);
		return openWebSocket.getOpenWebSocketId();
	}

	public void inactivateWebSocketSession(String webSocketSessionId) {
		openWebSocketRepository.inactivateByWebSocketId(webSocketSessionId);
	}
	
	public List<OpenWebSocket> getOpenWebSocketsForServer() {
		return openWebSocketRepository.getActiveByServerId(WebSocketServerId.getInstance().getId())
				.stream()
				.map(openWebSocketModel -> new OpenWebSocket(openWebSocketModel.getSessionId(), openWebSocketModel.getWebSocketId()))
				.collect(Collectors.toList());
	}
}
