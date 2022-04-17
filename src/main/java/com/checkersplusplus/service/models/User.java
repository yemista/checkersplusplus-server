package com.checkersplusplus.service.models;

public class User {
	private String id, email, password, alias;
	
	public User(String id, String email, String password, String alias) {
		super();
		this.id = id;
		this.email = email;
		this.password = password;
		this.alias = alias;
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
	
	
}
