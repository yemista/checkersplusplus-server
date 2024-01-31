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
@Table(name = "game_event")
public class GameEventModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID gameEventId;
	
	@Column(name = "event")
	private String event;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "event_recipient_account_id")
	private UUID eventRecipientAccountId;
	
	@Column(name = "game_id")
	private UUID gameId;
	
	@Column(name = "created")
	private LocalDateTime created;

	public GameEventModel(UUID gameEventId, String event, boolean active, UUID eventRecipientAccountId, UUID gameId, LocalDateTime created) {
		this.gameEventId = gameEventId;
		this.event = event;
		this.active = active;
		this.eventRecipientAccountId = eventRecipientAccountId;
		this.gameId = gameId;
		this.created = created;
	}

	public GameEventModel() {
	}

	public UUID getGameEventId() {
		return gameEventId;
	}

	public void setGameEventId(UUID gameEventId) {
		this.gameEventId = gameEventId;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public UUID getEventRecipientAccountId() {
		return eventRecipientAccountId;
	}

	public void setEventRecipientAccountId(UUID eventRecipientAccountId) {
		this.eventRecipientAccountId = eventRecipientAccountId;
	}

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}
	
}
