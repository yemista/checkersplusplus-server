package com.checkersplusplus.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkersplusplus.controllers.inputs.CreateUserInput;
import com.checkersplusplus.service.AccountService;


@RestController
@RequestMapping("/api/account")
public class AccountController {
	
	@Autowired
	private AccountService accountService;
	
	@GetMapping()
	public ResponseEntity test() {
		return ResponseEntity
    			.status(HttpStatus.OK)
    			.body("It works!");
	}

	@PostMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity createUser(@Valid @RequestBody CreateUserInput payload) {
        
        if (!accountService.isPasswordSafe(payload.getPassword())) {
        	return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Password must be at least 8 characters long and contain only letters and numbers");
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
        return ResponseEntity
        			.status(HttpStatus.OK)
                    .build();      	
	}
}
