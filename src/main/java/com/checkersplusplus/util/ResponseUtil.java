package com.checkersplusplus.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {

	public static ResponseEntity unknownError() {
		return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("An unknown error has occurred");
	}
}
