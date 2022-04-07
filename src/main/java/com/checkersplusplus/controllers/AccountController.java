package com.checkersplusplus.controllers;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkersplusplus.controllers.inputs.CreateUserInput;
import com.checkersplusplus.controllers.inputs.LoginInput;
import com.checkersplusplus.service.AccountService;


@RestController
@RequestMapping("/api/account")
public class AccountController {
	
	private static final Logger logger = Logger.getLogger(AccountController.class);
	
	@Autowired
	private AccountService accountService;
	
	@PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity login(@RequestBody LoginInput payload) {
		try {
			logger.debug("Attempting login for " + payload.getEmail());
			
			if (loginInputNotPopulated(payload)) {
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Please fill out all fields");
			}
			
			if (!accountService.isLoginValid(payload)) {
				return ResponseEntity
		    			.status(HttpStatus.BAD_REQUEST)
		    			.body("Invalid login");
			}
			
			String sessionId = accountService.login(payload);
			logger.debug("Successfully logged in account: " + payload.getEmail());
			logger.debug("Session id is: " + sessionId);
			return ResponseEntity
	    			.status(HttpStatus.OK)
	    			.eTag(sessionId)
	                .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during login: " + e.getMessage());
			e.printStackTrace();
			return unknownError();
		}
	}

	@PostMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUser(@RequestBody CreateUserInput payload) {
		try {
			logger.debug("Attempting create user for " + payload.getEmail());
			
			if (createUserInputNotPopulated(payload)) {
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Please fill out all fields");
			}
			
			if (!accountService.isEmailValid(payload.getEmail())) {
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Invalid email address");
			}
			
			if (!accountService.isAliasValid(payload.getAlias())) {
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Alias must be between 3 and 14 characters");
			}
	        
	        if (!accountService.isPasswordSafe(payload.getPassword())) {
	        	return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Password must contain only letters and numbers and be between 8 and 20 characters");
	        }
	        
	        if (!accountService.isAliasUnique(payload.getAlias())) {
	        	return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Provided alias is already in use");
	        }
	        
	        if (!accountService.isEmailUnique(payload.getEmail())) {
	        	return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Provided email address is already in use");
	        }
	        
	        accountService.createAccount(payload.getEmail(), payload.getPassword(), payload.getAlias());
	        logger.debug("Successfully created account for: " + payload.getEmail());
	        return ResponseEntity
	        			.status(HttpStatus.OK)
	                    .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create account: " + e.getMessage());
			return unknownError();
		}
	}

	private boolean loginInputNotPopulated(LoginInput payload) {
		return StringUtils.isBlank(payload.getEmail())
				|| StringUtils.isBlank(payload.getPassword());
	}

	private boolean createUserInputNotPopulated(CreateUserInput payload) {
		return StringUtils.isBlank(payload.getEmail()) 
				|| StringUtils.isBlank(payload.getAlias())
				|| StringUtils.isBlank(payload.getPassword());
	}
	
	private ResponseEntity unknownError() {
		return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("An unknown error has occurred");
	}
}
