package com.checklersplusplus.server.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "last_move_sent")
public class LastMoveSentModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID lastMoveSentId;
	
	@Column(name = "account_id")
	private UUID accountId;
	
	@Column(name = "game_id")
	private UUID gameId;
	
	@Column(name = "last_move_sent")
	private int lastMoveSent;

	public LastMoveSentModel(UUID lastMoveSentId, UUID accountId, UUID gameId, int lastMoveSent) {
		this.lastMoveSentId = lastMoveSentId;
		this.accountId = accountId;
		this.gameId = gameId;
		this.lastMoveSent = lastMoveSent;
	}

	public LastMoveSentModel() {
	}

	public UUID getLastMoveSentId() {
		return lastMoveSentId;
	}

	public void setLastMoveSentId(UUID lastMoveSentId) {
		this.lastMoveSentId = lastMoveSentId;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public UUID getGameId() {
		return gameId;
	}

	public void setGameId(UUID gameId) {
		this.gameId = gameId;
	}

	public int getLastMoveSent() {
		return lastMoveSent;
	}

	public void setLastMoveSent(int lastMoveSent) {
		this.lastMoveSent = lastMoveSent;
	}
}
