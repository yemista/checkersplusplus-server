package com.checklersplusplus.server.entities.internal;

import java.util.UUID;

public class OpenWebSocket {
	private UUID sessionId;
	private String webSocketSessionId;
	
	public OpenWebSocket(UUID sessionId, String webSocketSessionId) {
		this.sessionId = sessionId;
		this.webSocketSessionId = webSocketSessionId;
	}
	
	public UUID getSessionId() {
		return sessionId;
	}
	
	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}
	
	public String getWebSocketSessionId() {
		return webSocketSessionId;
	}
	
	public void setWebSocketSessionId(String webSocketSessionId) {
		this.webSocketSessionId = webSocketSessionId;
	}	
}
