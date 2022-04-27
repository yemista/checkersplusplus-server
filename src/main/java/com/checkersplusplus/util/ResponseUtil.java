package com.checkersplusplus.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.checkersplusplus.exceptions.CheckersPlusPlusException;
import com.checkersplusplus.service.models.CheckersPlusPlusError;

public class ResponseUtil {

	public static ResponseEntity unexpectedError(Exception exception) {
		if (exception instanceof CheckersPlusPlusException) {
			CheckersPlusPlusException checkersPlusPlusException = (CheckersPlusPlusException) exception;
			CheckersPlusPlusError error = new CheckersPlusPlusError(checkersPlusPlusException.getErrorCode());
			return ResponseEntity
	                .status(HttpStatus.BAD_REQUEST)
	                .body(error.convertToJson());
		}
		
		return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("An unknown error has occurred");
	}
}
