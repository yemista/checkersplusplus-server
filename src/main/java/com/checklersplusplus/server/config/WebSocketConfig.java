package com.checklersplusplus.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.checklersplusplus.server.websocket.CheckersPlusPlusWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer  {

	@Autowired
	private CheckersPlusPlusWebSocketHandler webSocketHandler;
	
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(webSocketHandler, "/checkersplusplus/api/updates").setAllowedOrigins("*");
		//      .addInterceptors(new HttpSessionHandshakeInterceptor());
	}

}
