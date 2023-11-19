package com.checklersplusplus.server.entities.response;

import java.io.Serializable;

public class CheckersPlusPlusResponse implements Serializable {
	private static final long serialVersionUID = 3913902219489451126L;

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
