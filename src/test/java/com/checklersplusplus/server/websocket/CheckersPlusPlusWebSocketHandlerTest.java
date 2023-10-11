package com.checklersplusplus.server.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CheckersPlusPlusWebSocketHandlerTest {
	
	@LocalServerPort
	private int port;
	
	@Test
	public void canConnectWithWebSocket() throws InterruptedException, ExecutionException, Exception {
		System.out.println("" + port);
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketSession webSocketSession = webSocketClient.execute(new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
                
            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                
            }
            
            @Override
        	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        		session.close();
        	}

        	@Override
        	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        		session.close();
        	}
        }, new WebSocketHttpHeaders(), URI.create(getWebSocketUrl())).get();
		UUID sessionId = UUID.randomUUID();
		webSocketSession.sendMessage(new TextMessage(sessionId.toString()));
		assertThat(webSocketSession.isOpen()).isTrue();
		webSocketSession.sendMessage(new TextMessage(UUID.randomUUID().toString()));
		webSocketSession.sendMessage(new TextMessage(UUID.randomUUID().toString()));
	}
	
	private String getWebSocketUrl() {
		return "ws://localhost:" + port + "/checkersplusplus/api/updates";
	}
}
