package com.checkersplusplus.service.models;

public class User {
	private String email, password, alias;
	
	public User(String email, String password, String alias) {
		super();
		this.email = email;
		this.password = password;
		this.alias = alias;
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
	
	
}
