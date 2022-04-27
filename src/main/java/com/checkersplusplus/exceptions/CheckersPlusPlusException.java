package com.checkersplusplus.exceptions;

public class CheckersPlusPlusException extends Exception {
	private int errorCode;
	
	protected CheckersPlusPlusException(String message, int errorCode) {
		super(String.format("%s: %d", message, errorCode));
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
