package com.checkersplusplus.service.models;

import com.checkersplusplus.service.enums.GameStatus;
import com.google.gson.annotations.Expose;

public class Game extends Jsonifiable {
	
	@Expose(serialize = false, deserialize = false)
	public String id;
	
	@Expose(serialize = true, deserialize = true)
	public String state;
	
	@Expose(serialize = true, deserialize = true)
	public GameStatus status;
	
	@Expose(serialize = true, deserialize = true)
	public String redId;
	
	@Expose(serialize = true, deserialize = true)
	public String blackId;

	public Game(String id, String state, GameStatus status, String redId, String blackId) {
		super();
		this.id = id;
		this.state = state;
		this.status = status;
		this.redId = redId;
		this.blackId = blackId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public String getRedId() {
		return redId;
	}

	public void setRedId(String redId) {
		this.redId = redId;
	}
	
	public String getBlackId() {
		return blackId;
	}

	public void setBlackId(String blackId) {
		this.blackId = blackId;
	}
}
