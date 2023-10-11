package com.checklersplusplus.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "open_web_socket")
public class OpenWebSocketModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID openWebSocketId;
	
	@Column(name = "created")
	private LocalDateTime created;
	
	@Column(name = "session_id")
	private UUID sessionId;
	
	@Column(name = "web_socket_id")
	private String webSocketId;
	
	@Column(name = "active")
	private boolean active;

	public OpenWebSocketModel(UUID openWebSocketId, String webSocketId, LocalDateTime created, UUID sessionId, boolean active) {
		this.openWebSocketId = openWebSocketId;
		this.created = created;
		this.sessionId = sessionId;
		this.active = active;
		this.webSocketId = webSocketId;
	}

	public OpenWebSocketModel() {
	}

	public UUID getOpenWebSocketId() {
		return openWebSocketId;
	}

	public void setOpenWebSocketId(UUID openWebSocketId) {
		this.openWebSocketId = openWebSocketId;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getWebSocketId() {
		return webSocketId;
	}

	public void setWebSocketId(String webSocketId) {
		this.webSocketId = webSocketId;
	}
}
