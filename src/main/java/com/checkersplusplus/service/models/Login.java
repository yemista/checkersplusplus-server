package com.checkersplusplus.service.models;

import com.google.gson.annotations.Expose;

public class Login extends Jsonifiable {

	@Expose(serialize = true, deserialize = true)
	public String userId;
	
	@Expose(serialize = true, deserialize = true)
	public String sessionId;

	public Login(String userId, String sessionId) {
		this.userId = userId;
		this.sessionId = sessionId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}
}
