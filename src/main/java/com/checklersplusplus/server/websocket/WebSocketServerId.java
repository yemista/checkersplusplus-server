package com.checklersplusplus.server.websocket;

import java.util.UUID;

public class WebSocketServerId {
	private static WebSocketServerId me = new WebSocketServerId();
	private static final UUID WEB_SOCKET_SERVER_ID = UUID.randomUUID();
	
	private WebSocketServerId() {
	}
	
	public static WebSocketServerId getInstance() {
		return me;
	}
	
	public UUID getId() {
		return WEB_SOCKET_SERVER_ID;
	}
}
