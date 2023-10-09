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
@Table(name = "game")
public class GameModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID gameId;
	
	@Column(name = "red_id")
	private UUID redId;
	
	@Column(name = "black_id")
	private UUID blackId;
	
	@Column(name = "winner_id")
	private UUID winnerId;
	
	@Column(name = "created")
	private LocalDateTime created;
	
	@Column(name = "last_modified")
	private LocalDateTime lastModified;
	
	@Column(name = "game_state")
	private String gameState;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "in_progress")
	private boolean inProgress;

	public GameModel(UUID gameId, UUID redId, UUID blackId, UUID winnerId, LocalDateTime created,
			LocalDateTime lastModified, String gameState, boolean active, boolean inProgress) {
		super();
		this.gameId = gameId;
		this.redId = redId;
		this.blackId = blackId;
		this.winnerId = winnerId;
		this.created = created;
		this.lastModified = lastModified;
		this.gameState = gameState;
		this.active = active;
		this.inProgress = inProgress;
	}

	public GameModel() {
	}

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	public UUID getRedId() {
		return redId;
	}

	public void setRedId(UUID redId) {
		this.redId = redId;
	}

	public UUID getBlackId() {
		return blackId;
	}

	public void setBlackId(UUID blackId) {
		this.blackId = blackId;
	}

	public UUID getWinnerId() {
		return winnerId;
	}

	public void setWinnerId(UUID winnerId) {
		this.winnerId = winnerId;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}

	public String getGameState() {
		return gameState;
	}

	public void setGameState(String gameState) {
		this.gameState = gameState;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isInProgress() {
		return inProgress;
	}

	public void setInProgress(boolean inProgress) {
		this.inProgress = inProgress;
	}
}
