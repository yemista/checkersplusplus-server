package com.checklersplusplus.server.entities.request;

import jakarta.validation.constraints.NotBlank;

public class Username {
	
	@NotBlank(message = "Username is required.")
	private String username;

	public Username(String username) {
		this.username = username;
	}

	public Username() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
