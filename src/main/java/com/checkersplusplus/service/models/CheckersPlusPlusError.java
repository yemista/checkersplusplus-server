package com.checkersplusplus.service.models;

import com.google.gson.annotations.Expose;

public class CheckersPlusPlusError extends Jsonifiable {

	@Expose(serialize = true, deserialize = true)
	public int errorCode;

	public CheckersPlusPlusError(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
