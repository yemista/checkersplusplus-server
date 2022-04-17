package com.checkersplusplus.service.models;

import java.util.Date;

public class Session {
	private String userId;
	private String tokenId;
	private Date heartbeat;
	
	public Session(String userId, String tokenId, Date heartbeat) {
		super();
		this.userId = userId;
		this.tokenId = tokenId;
		this.heartbeat = heartbeat;
	}

	public String getUserId() {
		return userId;
	}

	public String getTokenId() {
		return tokenId;
	}

	public Date getHeartbeat() {
		return heartbeat;
	}
}
