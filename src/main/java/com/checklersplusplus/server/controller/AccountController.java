package com.checklersplusplus.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checklersplusplus.server.entities.internal.NewAccount;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.request.Login;
import com.checklersplusplus.server.entities.request.ResetPassword;
import com.checklersplusplus.server.entities.request.Username;
import com.checklersplusplus.server.entities.request.VerifyAccount;
import com.checklersplusplus.server.entities.response.Account;
import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;
import com.checklersplusplus.server.entities.response.Session;
import com.checklersplusplus.server.exception.AccountNotVerifiedException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.UsernameNotFoundException;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.EmailService;
import com.checklersplusplus.server.service.VerificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/checkersplusplus/api/account")
public class AccountController {

	@Autowired
	private AccountService accountService;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private VerificationService verificationService;
	
	@PostMapping("/login")
	public ResponseEntity<Session> login(@Valid @RequestBody Login login) {
		try {
			Session session = accountService.login(login.getUsername(), login.getPassword());
			return new ResponseEntity<>(session, HttpStatus.OK);
		} catch (AccountNotVerifiedException a) {
			Session session = new Session();
			session.setMessage(a.getMessage());
			return new ResponseEntity<>(session, HttpStatus.FORBIDDEN);
		} catch (CheckersPlusPlusServerException e) {
			Session session = new Session();
			session.setMessage(e.getMessage());
			return new ResponseEntity<>(session, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			Session session = new Session();
			session.setMessage("Server error. Try again soon.");
			return new ResponseEntity<>(session, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/sendVerification")
	public ResponseEntity<CheckersPlusPlusResponse> sendVerificationCode(@Valid @RequestBody Username username) {
		try {
			Account account = accountService.findByUsername(username.getUsername());
			
			if (account == null) {
				throw new UsernameNotFoundException();
			}
			
			String verificationCode = verificationService.createVerificationCode(account.getAccountId());
			emailService.emailVerificationCode(account.getAccountId(), verificationCode);
		} catch(CheckersPlusPlusServerException e) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Server error. Try again soon."), HttpStatus.SERVICE_UNAVAILABLE);
		}
		return new ResponseEntity<>(new CheckersPlusPlusResponse("Please check your email for the verification code. If you do not see it check your spam folder."), HttpStatus.OK);
	}
	
	@PostMapping("/resetPassword")
	public ResponseEntity<CheckersPlusPlusResponse> resetPassword(@Valid @RequestBody ResetPassword resetPassword) {
		if (!isPasswordsMatch(resetPassword.getPassword(), resetPassword.getConfirmPassword())) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Password and confirmation password do not match."), HttpStatus.BAD_REQUEST);
		}
		
		try {
			accountService.resetPassword(resetPassword.getUsername(), resetPassword.getVerificationCode(), resetPassword.getPassword());
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse();
			response.setMessage("Password reset successful.");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (CheckersPlusPlusServerException e) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Server error. Try again soon."), HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/verify")
	public ResponseEntity<CheckersPlusPlusResponse> verifyAccount(@Valid @RequestBody VerifyAccount verifyAccount) {
		try {
			verificationService.verifyAccount(verifyAccount.getUsername(), verifyAccount.getVerificationCode());
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Account verified."), HttpStatus.OK);
		} catch (CheckersPlusPlusServerException e) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Failed to verify account. Please enter the most recent verification code."), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Server error. Try again soon."), HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/create")
	public ResponseEntity<CheckersPlusPlusResponse> createAccount(@Valid @RequestBody CreateAccount createAccount) {
		if (isEmailInUse(createAccount.getEmail())) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Email address is already in use."), HttpStatus.BAD_REQUEST);
		}
		
		if (isUsernameInUse(createAccount.getUsername())) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Username is already in use."), HttpStatus.BAD_REQUEST);
		}
		
		if (!isPasswordsMatch(createAccount.getPassword(), createAccount.getConfirmPassword())) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Password and confirmation password do not match."), HttpStatus.BAD_REQUEST);
		}
		
		try {
			NewAccount newAccount = accountService.createAccount(createAccount);
			emailService.emailVerificationCode(newAccount.getAccountId(), newAccount.getVerificationCode());
		} catch (CheckersPlusPlusServerException e) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Failed to create account. Please try again."), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Server error. Try again soon."), HttpStatus.SERVICE_UNAVAILABLE);
		}
		
		return new ResponseEntity<>(new CheckersPlusPlusResponse("Account created successfully. Please check your email for the verification code. If you do not see it check your spam folder."), HttpStatus.OK);
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
