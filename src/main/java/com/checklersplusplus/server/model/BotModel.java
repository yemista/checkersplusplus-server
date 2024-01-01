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
@Table(name = "bot")
public class BotModel {

	@Id
	@Column(name = "bot_id")
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID botId;
	
	@Column(name = "bot_account_id", unique = true)
	private UUID botAccountId;
	
	@Column(name = "in_use")
	private boolean inUse;
	
	@Column(name = "last_modified")
	private LocalDateTime lastModified;

	public UUID getBotId() {
		return botId;
	}

	public void setBotId(UUID botId) {
		this.botId = botId;
	}

	public UUID getBotAccountId() {
		return botAccountId;
	}

	public void setBotAccountId(UUID botAccountId) {
		this.botAccountId = botAccountId;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void setLastModified(LocalDateTime lastModified) {
		this.lastModified = lastModified;
	}
	
	
}
