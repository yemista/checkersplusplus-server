package com.checklersplusplus.server.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.model.OpenWebSocketModel;

@Service
@Transactional
public class OpenWebSocketService {
	
	private static final Logger logger = LoggerFactory.getLogger(OpenWebSocketService.class);

	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;

	public void createWebSocketSession(UUID serverSessionId, String webSocketSessionId) {
		OpenWebSocketModel openWebSocket = new OpenWebSocketModel();
		openWebSocket.setActive(true);
		openWebSocket.setCreated(LocalDateTime.now());
		openWebSocket.setWebSocketId(webSocketSessionId);
		openWebSocket.setSessionId(serverSessionId);
		openWebSocketRepository.save(openWebSocket);
	}


	public void inactivateWebSocketSession(String webSocketSessionId) {
		openWebSocketRepository.inactivateBySessionId(webSocketSessionId);
	}

	
}
