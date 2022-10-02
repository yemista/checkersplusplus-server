package com.checkersplusplus.service.models;

public class EmailVerification {
	private String email;
	private String code;
	
	public EmailVerification(String email, String code) {
		super();
		this.email = email;
		this.code = code;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}
}
