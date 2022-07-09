package com.checkersplusplus.dao.models;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "game")
public class GameModel implements Serializable {

	@Id
	@Column(name = "game_id", updatable = false, nullable = false)
    private String id;
	
	@Column(name = "red_id", updatable = false, nullable = false)
	private String redId;
	
	@Column(name = "black_id", updatable = true, nullable = true)
	private String blackId;
	
	@Column(name = "winner_id", updatable = true, nullable = true)
	private String winnerId;
	
	@Column(name = "forfeit", updatable = true, nullable = true)
	private String forfeitId;
	
	@Column(name = "state", updatable = true, nullable = false)
	private String state;
	
	@Column(name = "status", updatable = true, nullable = false)
	private String status;
	
	@Column(name = "created", updatable = false, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
    private Date created;
	
	@Column(name = "move_num", updatable = true, nullable = false)
	private Integer version;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getForfeitId() {
		return forfeitId;
	}

	public void setForfeitId(String forfeitId) {
		this.forfeitId = forfeitId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
}
