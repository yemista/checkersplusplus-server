package com.checkersplusplus.service.models;

import java.util.Objects;

import com.checkersplusplus.service.enums.GameStatus;
import com.google.gson.annotations.Expose;

public class Game extends Jsonifiable {
	
	@Expose(serialize = true, deserialize = true)
	public String id;
	
	@Expose(serialize = true, deserialize = true)
	public String state;
	
	@Expose(serialize = true, deserialize = true)
	public GameStatus status;
	
	@Expose(serialize = true, deserialize = true)
	public String redId;
	
	@Expose(serialize = true, deserialize = true)
	public String blackId;
	
	@Expose(serialize = true, deserialize = true)
	public String winnerId;
	
	@Expose(serialize = true, deserialize = true)
	public Integer version;

	public Game(String id, String state, GameStatus status, String redId, String blackId, String winnerId, Integer version) {
		super();
		this.id = id;
		this.state = state;
		this.status = status;
		this.redId = redId;
		this.blackId = blackId;
		this.winnerId = winnerId;
		this.version = version;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state.replaceFirst("N", version.toString());
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

	public String getWinnerId() {
		return winnerId;
	}

	public void setWinnerId(String winnerId) {
		this.winnerId = winnerId;
	}
	
	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof Game) {
			Game other = (Game) o;
			return Objects.equals(other.id, this.id)
					&& Objects.equals(other.blackId, this.blackId)
					&& Objects.equals(other.redId, this.redId)
					&& Objects.equals(other.state, this.state)
					&& Objects.equals(other.winnerId, this.winnerId)
					&& other.status == this.status
					&& Objects.equals(other.version, this.version);
		}
	
		return false;
	}
}
