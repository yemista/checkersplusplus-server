package com.checklersplusplus.server.entities.request;

import jakarta.validation.constraints.NotBlank;

public class Login {
	
	@NotBlank(message = "Username is required.")
	private String username;
	
	@NotBlank(message = "Password is required.")
	private String password;
	
	public Login(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public Login() {
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
