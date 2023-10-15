package com.checklersplusplus.server.websocket;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.model.OpenWebSocketModel;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@RunWith(SpringRunner.class)
public class CheckersPlusPlusWebSocketHandlerTest {
	
	@LocalServerPort
	private int port;
	
	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;
	
	@Test
	public void canConnectWithWebSocket() throws InterruptedException, ExecutionException, Exception {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		WebSocketSession webSocketSession = webSocketClient.execute(new TextWebSocketHandler() {
            @Override
            public void handleTextMessage(WebSocketSession session, TextMessage message) {
            	System.out.println("message");
            }

            @Override
            public void afterConnectionEstablished(WebSocketSession session) {
                
            	System.out.println("connection opened");
            }
            
            @Override
        	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        		System.out.println("transport error");
            	session.close();
        	}

        	@Override
        	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        		System.out.println("connection close");
        		session.close();
        	}
        }, new WebSocketHttpHeaders(), URI.create(getWebSocketUrl())).get();
		UUID serverSessionId = UUID.randomUUID();
		webSocketSession.sendMessage(new TextMessage(serverSessionId.toString()));
		assertThat(webSocketSession.isOpen()).isTrue();
		
		Thread.sleep(100);
		Optional<OpenWebSocketModel> openWebSocketModel = openWebSocketRepository.getActiveByServerSessionId(serverSessionId);
		assertThat(openWebSocketModel.isPresent()).isTrue();

		UUID secondServerSessionId = UUID.randomUUID();
		webSocketSession.sendMessage(new TextMessage(secondServerSessionId.toString()));
		Thread.sleep(100);  // Sleep here to make sure secondServerSessionId makes it to database before querying it.
		Optional<OpenWebSocketModel> secondOpenWebSocketModel = openWebSocketRepository.getActiveByServerSessionId(secondServerSessionId);
		assertThat(secondOpenWebSocketModel.isPresent()).isTrue();
		assertThat(secondOpenWebSocketModel.get().getOpenWebSocketId()).isNotEqualTo(openWebSocketModel.get().getOpenWebSocketId());
	}
	
	private String getWebSocketUrl() {
		return "ws://localhost:" + port + "/checkersplusplus/api/updates";
	}
}
