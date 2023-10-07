package com.checklersplusplus.server.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class ValidationExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> notValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<String> errors = new ArrayList<>();
		ex.getAllErrors().forEach(err -> errors.add(err.getDefaultMessage()));
		return new ResponseEntity<>(new CheckersPlusPlusResponse(errors.get(0)), HttpStatus.BAD_REQUEST);
	}

}
