package com.checklersplusplus.server.exception;

public class UsernameNotFoundException extends CheckersPlusPlusServerException {

	public UsernameNotFoundException() {
		super("Username not found.");
	}

}
