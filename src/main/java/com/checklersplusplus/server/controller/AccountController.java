package com.checklersplusplus.server.controller;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checklersplusplus.server.dao.AccountRepository;
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
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.service.AccountService;
import com.checklersplusplus.server.service.VerificationService;
import com.checklersplusplus.server.service.mail.EmailService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/checkersplusplus/api/account")
public class AccountController {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
	private static final String VERSION_STRING = "1.0";

	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private VerificationService verificationService;
	
	@GetMapping("/version")
	public ResponseEntity<String> getVersion() {
		logger.info(String.format("Version request"));
		return new ResponseEntity<>(VERSION_STRING, HttpStatus.OK);
	}
	
	@GetMapping("/username")
	public ResponseEntity<String> getUsername(@RequestParam(required = false) String email) {
		if (email != null && !email.isBlank()) {
			Account account = accountService.findByEmail(email);
			
			if (account != null) {
				emailService.sendSimpleMessage(email, "Checkers++ username request", "Your Checkers++ username is: " + account.getUsername());
			}
		}
		
		return new ResponseEntity<>("", HttpStatus.OK);
	}
	
	@GetMapping("/tutorial/{accountId}")
	public ResponseEntity<String> disableTutorial(@PathVariable("accountId") UUID accountId) {
	    Optional<AccountModel> account = accountRepository.findById(accountId);
	    account.get().setTutorial(false);
	    accountRepository.save(account.get());
	    return new ResponseEntity<>("OK", HttpStatus.OK);
	}
	
	@PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Session> login(@Valid @RequestBody Login login) {
		try {
			Session session = accountService.login(login.getUsername(), login.getPassword());
			logger.info(String.format("Successful login by %s", login.getUsername()));
			return new ResponseEntity<>(session, HttpStatus.OK);
		} catch (AccountNotVerifiedException a) {
			logger.info(a.getMessage());
			Session session = new Session();
			session.setMessage(a.getMessage());
			return new ResponseEntity<>(session, HttpStatus.FORBIDDEN);
		} catch (CheckersPlusPlusServerException e) {
			logger.info(e.getMessage());
			Session session = new Session();
			session.setMessage(e.getMessage());
			return new ResponseEntity<>(session, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
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
			logger.info(e.getMessage());
			return new ResponseEntity<>(new CheckersPlusPlusResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Server error. Try again soon."), HttpStatus.SERVICE_UNAVAILABLE);
		}
		return new ResponseEntity<>(new CheckersPlusPlusResponse("Please check your email for the verification code. If you do not see it check your spam folder. If you still do not see it, try to request a new one."), HttpStatus.OK);
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
			logger.info(e.getMessage());
			return new ResponseEntity<>(new CheckersPlusPlusResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Server error. Try again soon."), HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/verify")
	public ResponseEntity<CheckersPlusPlusResponse> verifyAccount(@Valid @RequestBody VerifyAccount verifyAccount) {
		try {
			verificationService.verifyAccount(verifyAccount.getUsername(), verifyAccount.getVerificationCode());
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Account verified. Please log in."), HttpStatus.OK);
		} catch (CheckersPlusPlusServerException e) {
			logger.info(e.getMessage());
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Failed to verify account. Please enter the most recent verification code."), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Server error. Try again soon."), HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/create")
	public ResponseEntity<CheckersPlusPlusResponse> createAccount(@Valid @RequestBody CreateAccount createAccount) {
		try {
			if (isEmailInUse(createAccount.getEmail())) {
				return new ResponseEntity<>(new CheckersPlusPlusResponse("Email address is already in use."), HttpStatus.BAD_REQUEST);
			}

			if (isUsernameInUse(createAccount.getUsername())) {
				return new ResponseEntity<>(new CheckersPlusPlusResponse("Username is already in use."), HttpStatus.BAD_REQUEST);
			}

			if (!isPasswordsMatch(createAccount.getPassword(), createAccount.getConfirmPassword())) {
				return new ResponseEntity<>(new CheckersPlusPlusResponse("Password and confirmation password do not match."), HttpStatus.BAD_REQUEST);
			}

			NewAccount newAccount = accountService.createAccount(createAccount);
			logger.debug(String.format("Created new account %s", createAccount.getUsername()));
			emailService.emailVerificationCode(newAccount.getAccountId(), newAccount.getVerificationCode());
		} catch (CheckersPlusPlusServerException e) {
			logger.info(e.getMessage(), e);
			return new ResponseEntity<>(new CheckersPlusPlusResponse("Failed to create account. Please try again."), HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage(), ex);
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
