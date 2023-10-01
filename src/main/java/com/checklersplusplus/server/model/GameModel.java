package com.checklersplusplus.server.model;

import java.sql.Timestamp;
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
	private Timestamp created;
	
	@Column(name = "last_modified")
	private Timestamp lastModified;
	
	@Column(name = "game_state")
	private String gameState;
	
	@Column(name = "active")
	private boolean active;

	public GameModel(UUID gameId, UUID redId, UUID blackId, UUID winnerId, Timestamp created,
			Timestamp lastModified, String gameState, boolean active) {
		super();
		this.gameId = gameId;
		this.redId = redId;
		this.blackId = blackId;
		this.winnerId = winnerId;
		this.created = created;
		this.lastModified = lastModified;
		this.gameState = gameState;
		this.active = active;
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

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
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

}
