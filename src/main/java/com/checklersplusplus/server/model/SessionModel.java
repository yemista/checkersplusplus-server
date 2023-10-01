package com.checklersplusplus.server.model;

import java.sql.Timestamp;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "session")
public class SessionModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID sessionId;
	
	@Column(name = "account_id")
	private UUID accountId;
	
	@Column(name = "active")
	private boolean active;
	
	@Column(name = "last_modified")
	private Timestamp lastModified;

	public SessionModel() {
	}
	
	public SessionModel(UUID sessionId, UUID accountId, boolean active, Timestamp lastModified) {
		this.sessionId = sessionId;
		this.accountId = accountId;
		this.active = active;
		this.lastModified = lastModified;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public void setSessionId(UUID sessionId) {
		this.sessionId = sessionId;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Timestamp getLastModified() {
		return lastModified;
	}

	public void setLastModified(Timestamp lastModified) {
		this.lastModified = lastModified;
	}
}
