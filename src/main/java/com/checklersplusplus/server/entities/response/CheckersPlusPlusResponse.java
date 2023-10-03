package com.checklersplusplus.server.entities.response;

public class CheckersPlusPlusResponse {
	private String message;

	public CheckersPlusPlusResponse() {
	}
	
	public CheckersPlusPlusResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
