package com.checkersplusplus.dao.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "active_games")
public class ActiveGameModel {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "active_game_id", updatable = false, nullable = false)
    private Long id;
	
	@Column(name = "user_id", updatable = false, nullable = false, unique = true)
	private String userId;
	
	@Column(name = "game_id", updatable = false, nullable = false)
	private String gameId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getGameId() {
		return gameId;
	}

	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
}
