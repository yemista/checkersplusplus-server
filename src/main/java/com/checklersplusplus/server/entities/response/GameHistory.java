package com.checklersplusplus.server.entities.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

import com.checklersplusplus.server.model.GameModel;

public class GameHistory extends CheckersPlusPlusResponse implements Serializable {
	private UUID gameId;
	private UUID redAccountId;
	private UUID blackAccountId;
	private Integer redRating;
	private Integer blackRating;
	private LocalDateTime completedTime;
	private String redUsername;
	private String blackUsername;
	
	private GameHistory(UUID gameId, UUID redAccountId, UUID blackAccountId, Integer redRating, Integer blackRating,
			LocalDateTime completedTime) {
		this.gameId = gameId;
		this.redAccountId = redAccountId;
		this.blackAccountId = blackAccountId;
		this.redRating = redRating;
		this.blackRating = blackRating;
		this.completedTime = completedTime;
	}
	
	public GameHistory() {
	}

	public static GameHistory fromModel(GameModel model) {
		return new GameHistory(model.getGameId(), model.getRedId(), model.getBlackId(), model.getRedRating(), model.getBlackRating(), model.getLastModified());
	}

	public UUID getGameId() {
		return gameId;
	}
	
	public UUID getRedAccountId() {
		return redAccountId;
	}
	
	public UUID getBlackAccountId() {
		return blackAccountId;
	}
	
	public Integer getRedRating() {
		return redRating;
	}
	
	public Integer getBlackRating() {
		return blackRating;
	}
	
	public LocalDateTime getCompletedTime() {
		return completedTime;
	}

	public String getRedUsername() {
		return redUsername;
	}

	public void setRedUsername(String redUsername) {
		this.redUsername = redUsername;
	}

	public String getBlackUsername() {
		return blackUsername;
	}

	public void setBlackUsername(String blackUsername) {
		this.blackUsername = blackUsername;
	}
}
