package com.checklersplusplus.server.websocket;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.data.util.Pair;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketMap {

	private static final Map<String, Pair<WebSocketSession, UUID>> idToActiveSession = new ConcurrentHashMap<>();
	private static WebSocketMap me = new WebSocketMap();
	
	private WebSocketMap() {
	}
	
	public static WebSocketMap getInstance() {
		return me;
	}
	
	public Map<String, Pair<WebSocketSession, UUID>> getMap() {
		return idToActiveSession;
	}
}
