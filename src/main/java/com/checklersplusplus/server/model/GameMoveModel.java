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
@Table(name = "game_move")
public class GameMoveModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID gameMoveId;
	
	@Column(name = "account_id")
	private UUID accountId;
	
	@Column(name = "game_id")
	private UUID gameId;
	
	@Column(name = "move_list")
	private String moveList;
	
	@Column(name = "created")
	private LocalDateTime created;
	
	@Column(name = "move_number")
	private int moveNumber;
	
	// state before this move is applied
	@Column(name = "board_state")
	private String boardState;

	public GameMoveModel(UUID gameMoveId, UUID accountId, UUID gameId, String moveList, LocalDateTime created,
			int moveNumber) {
		this.gameMoveId = gameMoveId;
		this.accountId = accountId;
		this.gameId = gameId;
		this.moveList = moveList;
		this.created = created;
		this.moveNumber = moveNumber;
	}

	public GameMoveModel() {
	}

	public UUID getGameMoveId() {
		return gameMoveId;
	}

	public void setGameMoveId(UUID gameMoveId) {
		this.gameMoveId = gameMoveId;
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

	public String getMoveList() {
		return moveList;
	}

	public void setMoveList(String moveList) {
		this.moveList = moveList;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public int getMoveNumber() {
		return moveNumber;
	}

	public void setMoveNumber(int moveNumber) {
		this.moveNumber = moveNumber;
	}

	public String getBoardState() {
		return boardState;
	}

	public void setBoardState(String boardState) {
		this.boardState = boardState;
	}
}
