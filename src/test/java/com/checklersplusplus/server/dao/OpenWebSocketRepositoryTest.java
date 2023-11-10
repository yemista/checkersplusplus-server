package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.model.OpenWebSocketModel;
import com.checklersplusplus.server.websocket.WebSocketServerId;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class OpenWebSocketRepositoryTest {
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;
	
	@Test
	public void canInactivateByWebSocketSessionId() {
		String webSocketSessionId = UUID.randomUUID().toString();
		createWebSocketSession(UUID.randomUUID(), webSocketSessionId);
		Optional<OpenWebSocketModel> findBeforeInactive = openWebSocketRepository.findByWebSocketId(webSocketSessionId);
		assertThat(findBeforeInactive.isPresent()).isTrue();
		assertThat(findBeforeInactive.get().isActive()).isTrue();
		
		openWebSocketRepository.inactivateByWebSocketId(webSocketSessionId);
		
		Optional<OpenWebSocketModel> findAfterInactive = openWebSocketRepository.findByWebSocketId(webSocketSessionId);
		assertThat(findAfterInactive.isPresent()).isTrue();
		
		// for some reason the next line fails...
		assertThat(findAfterInactive.get().isActive()).isFalse();
	}
	
	private void createWebSocketSession(UUID serverSessionId, String webSocketSessionId) {
		OpenWebSocketModel openWebSocket = new OpenWebSocketModel();
		openWebSocket.setActive(true);
		openWebSocket.setCreated(LocalDateTime.now());
		openWebSocket.setWebSocketId(webSocketSessionId);
		openWebSocket.setSessionId(serverSessionId);
		openWebSocket.setServerId(WebSocketServerId.getInstance().getId());
		openWebSocketRepository.save(openWebSocket);
	}
}
