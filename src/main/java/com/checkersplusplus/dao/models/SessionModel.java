package com.checkersplusplus.dao.models;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "sessions")
public class SessionModel {

	@Id
	@Column(name = "session_id", updatable = false, nullable = false)
    private Long id;
	
	@Column(name = "user_id", updatable = false, nullable = false)
	private String userId;
	
	@Column(name = "token", updatable = false, nullable = false)
	private String token;
	
	@Column(name = "active", updatable = true, nullable = false)
	private Boolean active;
	
	@Column(name = "heartbeat", updatable = true, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
    private Date heartbeat;
	
	@Column(name = "create_date", updatable = false, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(Date heartbeat) {
		this.heartbeat = heartbeat;
	}	
}
