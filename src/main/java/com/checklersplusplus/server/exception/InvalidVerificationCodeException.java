package com.checklersplusplus.server.exception;

public class InvalidVerificationCodeException extends CheckersPlusPlusServerException {

	public InvalidVerificationCodeException() {
		super("Invalid verification code. Check your email for the most recent code.");
	}

}
