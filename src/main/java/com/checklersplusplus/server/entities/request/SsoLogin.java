package com.checklersplusplus.server.entities.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotEmpty;

public class SsoLogin implements Serializable {
	@NotEmpty(message = "Email is required.")
	private String ssoEmail;

	public SsoLogin(String ssoEmail) {
		super();
		this.ssoEmail = ssoEmail;
	}
	
	public SsoLogin() {
		
	}

	public String getSsoEmail() {
		return ssoEmail;
	}

	public void setSsoEmail(String ssoEmail) {
		this.ssoEmail = ssoEmail;
	}
	
	
}
