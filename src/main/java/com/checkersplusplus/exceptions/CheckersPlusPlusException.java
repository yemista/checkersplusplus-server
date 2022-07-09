package com.checkersplusplus.exceptions;

import com.checkersplusplus.service.models.CheckersPlusPlusError;

public class CheckersPlusPlusException extends Exception {
	private CheckersPlusPlusError error;
	
	public CheckersPlusPlusException(CheckersPlusPlusError error) {
		super(String.format("%s", error.convertToJson()));
		this.error = error;
	}
	
	public CheckersPlusPlusError getError() {
		return error;
	}
}
