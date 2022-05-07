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
import com.checkersplusplus.exceptions.ErrorCodes;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.util.ResponseUtil;


@RestController
@RequestMapping("/api/account")
public class AccountController {
	
	private static final Logger logger = Logger.getLogger(AccountController.class);
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	@PostMapping(value = "login", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity login(@RequestBody LoginInput payload) {
		try {
			logger.debug("Attempting login for " + payload.getEmail());
			
			if (loginInputNotPopulated(payload)) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_LOGIN);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			if (!accountService.isLoginValid(payload.getEmail(), payload.getPassword())) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_LOGIN);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			Login login = accountService.login(payload.getEmail());
			
			// TODO - do we need this?
			if (gameService.hasActiveGame(login.getUserId())) {
				deleteActiveGameIfObsolete();
			}
			
			logger.debug("Successfully logged in account: " + payload.getEmail());
			logger.debug("Session id is: " + login.getSessionId());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	    			.body(login.convertToJson());
		} catch (Exception e) {
			logger.debug("Exception occurred during login: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unexpectedError(e);
		}
	}

	@PostMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUser(@RequestBody CreateUserInput payload) {
		try {
			logger.debug("Attempting create user for " + payload.getEmail());
			
			if (createUserInputNotPopulated(payload)) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_CREATE_USER_INPUT);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			if (!accountService.isEmailValid(payload.getEmail())) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_EMAIL);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			if (!accountService.isAliasValid(payload.getAlias())) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_ALIAS);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
	        
	        if (!accountService.isPasswordSafe(payload.getPassword())) {
	        	CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_PASSWORD);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
	        }
	        
	        if (!accountService.isAliasUnique(payload.getAlias())) {
	        	CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_ALIAS_NON_UNIQUE);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
	        }
	        
	        if (!accountService.isEmailUnique(payload.getEmail())) {
	        	CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_ALIAS_IN_USE);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
	        }
	        
	        accountService.createAccount(payload.getEmail(), payload.getPassword(), payload.getAlias());
	        logger.debug("Successfully created account for: " + payload.getEmail());
	        return ResponseEntity
	        			.status(HttpStatus.OK)
	                    .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create account: " + e.getMessage());
			return ResponseUtil.unexpectedError(e);
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
	
	private void deleteActiveGameIfObsolete() {
		// TODO Auto-generated method stub
		
	}
}
