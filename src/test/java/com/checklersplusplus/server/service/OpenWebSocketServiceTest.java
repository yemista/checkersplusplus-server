package com.checklersplusplus.server.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.OpenWebSocketRepository;
import com.checklersplusplus.server.entities.internal.OpenWebSocket;
import com.checklersplusplus.server.model.OpenWebSocketModel;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OpenWebSocketServiceTest {

	@Autowired
	private OpenWebSocketService openWebSocketService;
	
	@Autowired
	private OpenWebSocketRepository openWebSocketRepository;
	
	private List<OpenWebSocketModel> openWebSocketsToDelete = new ArrayList<>();
	
	@After
	public void cleanupDatabaseObjects() {
		openWebSocketsToDelete.forEach(openWebSocket -> openWebSocketRepository.delete(openWebSocket));
	}
	
	@Test
	public void canCreateWebSocketSession() {
		UUID serverSessionId = UUID.randomUUID();
		String webSocketSessionId = UUID.randomUUID().toString();
		openWebSocketService.createWebSocketSession(serverSessionId, webSocketSessionId);
		Optional<OpenWebSocketModel> model = openWebSocketRepository.getActiveByServerSessionId(serverSessionId);		
		assertThat(model.isPresent()).isTrue();
		openWebSocketsToDelete.add(model.get());
		assertThat(model.get().getWebSocketId()).isEqualTo(webSocketSessionId);
		assertThat(model.get().getSessionId()).isEqualTo(serverSessionId);
		assertThat(model.get().isActive()).isTrue();
	}
	
	@Test
	public void canInactivateWebSocketSession() {
		UUID serverSessionId = UUID.randomUUID();
		String webSocketSessionId = UUID.randomUUID().toString();
		openWebSocketService.createWebSocketSession(serverSessionId, webSocketSessionId);
		Optional<OpenWebSocketModel> model = openWebSocketRepository.getActiveByServerSessionId(serverSessionId);		
		assertThat(model.isPresent()).isTrue();
		openWebSocketsToDelete.add(model.get());
		assertThat(model.get().isActive()).isTrue();
		
		openWebSocketService.inactivateWebSocketSession(webSocketSessionId);
		Optional<OpenWebSocketModel> updatedModel = openWebSocketRepository.getActiveByServerSessionId(serverSessionId);		
		assertThat(updatedModel.isPresent()).isFalse();
		
		Optional<OpenWebSocketModel> inactiveModel = openWebSocketRepository.findById(model.get().getOpenWebSocketId());
		assertThat(inactiveModel.get().isActive()).isFalse();
	}
	
	@Test
	public void canGetOpenWebSocketsForServer() {
		UUID serverSessionId = UUID.randomUUID();
		String webSocketSessionId = UUID.randomUUID().toString();
		openWebSocketService.createWebSocketSession(serverSessionId, webSocketSessionId);
		Optional<OpenWebSocketModel> model = openWebSocketRepository.getActiveByServerSessionId(serverSessionId);		
		assertThat(model.isPresent()).isTrue();
		openWebSocketsToDelete.add(model.get());
		
		List<OpenWebSocket> openWebSockets = openWebSocketService.getOpenWebSocketsForServer();
		
		assertThat(openWebSockets.size()).isEqualTo(1);
		assertThat(openWebSockets.get(0).getWebSocketSessionId()).isEqualTo(webSocketSessionId);
		assertThat(openWebSockets.get(0).getSessionId()).isEqualTo(serverSessionId);
	}
}
