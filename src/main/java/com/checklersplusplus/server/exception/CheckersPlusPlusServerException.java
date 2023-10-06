package com.checklersplusplus.server.exception;

public class CheckersPlusPlusServerException extends Exception {
	private String message;
	
	public CheckersPlusPlusServerException(String message) {
		super(message);
		this.message = message;
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
