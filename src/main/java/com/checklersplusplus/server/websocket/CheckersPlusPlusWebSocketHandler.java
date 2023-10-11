package com.checklersplusplus.server.websocket;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.checklersplusplus.server.service.OpenWebSocketService;

/**
 * The overall web socket strategy is as follows:
 * 
 * 1. Upon connection established, store the web socket session id in our web socket map.
 * 2. 
 */
@Component
public class CheckersPlusPlusWebSocketHandler extends TextWebSocketHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(CheckersPlusPlusWebSocketHandler.class);
	
	private static final UUID SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	
	@Autowired
	private OpenWebSocketService openWebSocketService;
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		WebSocketMap.getInstance().getMap().put(session.getId(), Pair.of(session, SYSTEM_ID));
		super.afterConnectionEstablished(session);
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String serverSession = message.getPayload();
		
		try {
			UUID serverSessionId = UUID.fromString(serverSession);
			Pair<WebSocketSession, UUID> pair = WebSocketMap.getInstance().getMap().get(session.getId());
			
			if (pair == null || pair.getSecond().equals(SYSTEM_ID) || !pair.getSecond().equals(serverSessionId)) {
				openWebSocketService.inactivateWebSocketSession(session.getId());
				WebSocketMap.getInstance().getMap().put(session.getId(), Pair.of(session, serverSessionId));
				openWebSocketService.createWebSocketSession(serverSessionId, session.getId());
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		openWebSocketService.inactivateWebSocketSession(session.getId());
		WebSocketMap.getInstance().getMap().remove(session.getId());
		session.close();
        super.handleTransportError(session, exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		openWebSocketService.inactivateWebSocketSession(session.getId());
		WebSocketMap.getInstance().getMap().remove(session.getId());
		session.close();
        super.afterConnectionClosed(session, closeStatus);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

}
