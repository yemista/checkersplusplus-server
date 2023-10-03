package com.checklersplusplus.server.entities.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class Login {
	
	@Size(min = 3, max = 20, message = "The username must be from 3 to 20 characters.")
	@NotBlank(message = "The username is required.")
	private String username;
	
	@NotBlank(message = "The password is required.")
	private String password;
	
	public Login(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
