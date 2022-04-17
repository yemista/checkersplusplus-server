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
	public String nextToAct;

	public Game(String id, String state, GameStatus status, String nextToAct) {
		super();
		this.id = id;
		this.state = state;
		this.status = status;
		this.nextToAct = nextToAct;
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

	public String getNextToAct() {
		return nextToAct;
	}

	public void setNextToAct(String nextToAct) {
		this.nextToAct = nextToAct;
	}
}
