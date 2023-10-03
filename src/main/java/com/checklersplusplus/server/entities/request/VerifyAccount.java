package com.checklersplusplus.server.entities.request;

public class VerifyAccount {
	private String username;
	private String verificationCode;
	
	public VerifyAccount(String username, String verificationCode) {
		super();
		this.username = username;
		this.verificationCode = verificationCode;
	}

	public VerifyAccount() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
}
