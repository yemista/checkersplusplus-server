package com.checklersplusplus.server.entities;

public class VerifyAccount {
	private String email;
	private String verificationCode;
	
	public VerifyAccount(String email, String verificationCode) {
		super();
		this.email = email;
		this.verificationCode = verificationCode;
	}

	public VerifyAccount() {
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}
}
