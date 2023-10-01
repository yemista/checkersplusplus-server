package com.checklersplusplus.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checklersplusplus.server.entities.CreateAccount;
import com.checklersplusplus.server.entities.Login;
import com.checklersplusplus.server.entities.Session;
import com.checklersplusplus.server.entities.VerifyAccount;
import com.checklersplusplus.server.exception.AccountNotVerifiedException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/checkersplusplus/api/account")
public class AccountController {

	@Autowired
	private AccountService accountService;
	
	@PostMapping("/login")
	public ResponseEntity<Session> login(@Valid @RequestBody Login login) {
		try {
			Session session = accountService.login(login);
			return new ResponseEntity<>(session, HttpStatus.OK);
		} catch (AccountNotVerifiedException a) {
			Session session = new Session();
			session.setMessage(a.getMessage());
			return new ResponseEntity<>(session, HttpStatus.FORBIDDEN);
		} catch (CheckersPlusPlusServerException e) {
			Session session = new Session();
			session.setMessage(e.getMessage());
			return new ResponseEntity<>(session, HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("/resendVerification")
	public ResponseEntity<String> resendVerificationCode() {
		// TODO - implement
		return new ResponseEntity<>("Please check your email for the verification code. If you do not see it check your spam folder.", HttpStatus.OK);
	}
	
	@PostMapping("/verify")
	public ResponseEntity<String> verifyAccount(@Valid @RequestBody VerifyAccount verifyAccount) {
		try {
			accountService.verifyAccount(verifyAccount);
			return new ResponseEntity<>("Account verified.", HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>("Failed to verify account. Please enter the most recent verification code.", HttpStatus.BAD_REQUEST);
		}
	}
	
	@PostMapping("/create")
	public ResponseEntity<String> createAccount(@Valid @RequestBody CreateAccount createAccount) {
		if (isEmailInUse(createAccount.getEmail())) {
			return new ResponseEntity<>("Email address is already in use.", HttpStatus.BAD_REQUEST);
		}
		
		if (isUsernameInUse(createAccount.getUsername())) {
			return new ResponseEntity<>("Username is already in use.", HttpStatus.BAD_REQUEST);
		}
		
		if (!isPasswordsMatch(createAccount.getPassword(), createAccount.getConfirmPassword())) {
			return new ResponseEntity<>("Password and confirmation password do not match.", HttpStatus.BAD_REQUEST);
		}
		
		try {
			accountService.createAccount(createAccount);
		} catch (Exception e) {
			return new ResponseEntity<>("Failed to create account. Please try again.", HttpStatus.BAD_REQUEST);
		}
		
		return new ResponseEntity<>("Account created successfully. Please check your email for the verification code. If you do not see it check your spam folder.", HttpStatus.OK);
	}

	private boolean isPasswordsMatch(String password, String confirmPassword) {
		return password.equals(confirmPassword);
	}

	private boolean isUsernameInUse(String username) {
		return accountService.findByUsername(username) != null;
	}

	private boolean isEmailInUse(String email) {
		return accountService.findByEmail(email) != null;
	}
}
