package com.checklersplusplus.server.model;

import java.time.LocalDate;
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

	@Column(name = "creator_rating")
	private Integer creatorRating;
	
	@Column(name = "red_rating")
	private Integer redRating;
	
	@Column(name = "black_rating")
	private Integer blackRating;
	
	@Column(name = "winner_id")
	private UUID winnerId;
	
	// TODO add db index on created
	@Column(name = "created")
	private LocalDate created;
	
	@Column(name = "last_modified")
	private LocalDateTime lastModified;
	
	@Column(name = "game_state")
	private String gameState;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "in_progress")
	private boolean inProgress;
	
	@Column(name = "current_move_number")
	private int currentMoveNumber;
	
	@Column(name = "finalized")
	private boolean finalized;

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

	public LocalDate getCreated() {
		return created;
	}

	public void setCreated(LocalDate created) {
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

	public int getCurrentMoveNumber() {
		return currentMoveNumber;
	}

	public void setCurrentMoveNumber(int currentMoveNumber) {
		this.currentMoveNumber = currentMoveNumber;
	}

	public Integer getRedRating() {
		return redRating;
	}

	public void setRedRating(Integer redRating) {
		this.redRating = redRating;
	}

	public Integer getBlackRating() {
		return blackRating;
	}

	public void setBlackRating(Integer blackRating) {
		this.blackRating = blackRating;
	}

	public Integer getCreatorRating() {
		return creatorRating;
	}

	public void setCreatorRating(Integer creatorRating) {
		this.creatorRating = creatorRating;
	}

	public boolean isFinalized() {
		return finalized;
	}

	public void setFinalized(boolean finalized) {
		this.finalized = finalized;
	}
	
	
}
