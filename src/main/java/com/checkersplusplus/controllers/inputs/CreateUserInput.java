package com.checkersplusplus.controllers.inputs;

public class CreateUserInput {
	
	private String email;
	private String alias;
	private String password;

	public CreateUserInput(String email, String alias, String password) {
		this.email = email;
		this.alias = alias;
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
