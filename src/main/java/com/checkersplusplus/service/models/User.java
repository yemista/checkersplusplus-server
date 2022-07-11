package com.checkersplusplus.service.models;

public class User {
	private String id, email, password, alias;
	private int verified;
	
	public User(String id, String email, String password, String alias, int verified) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.alias = alias;
		this.verified = verified;
	}
	
	public String getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public String getAlias() {
		return alias;
	}

	public int getVerified() {
		return verified;
	}
	
	
}
