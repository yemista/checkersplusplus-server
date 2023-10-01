package com.checklersplusplus.server.exception;

public class SessionNotFoundException extends CheckersPlusPlusServerException  {

	public SessionNotFoundException() {
		super("Session not found. Please login.");
	}
	
}
