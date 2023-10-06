package com.checklersplusplus.server.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.internal.NewAccount;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.response.Account;
import com.checklersplusplus.server.entities.response.Session;
import com.checklersplusplus.server.exception.AccountNotVerifiedException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.InvalidVerificationCodeException;
import com.checklersplusplus.server.exception.UsernameNotFoundException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.model.VerifyAccountModel;
import com.checklersplusplus.server.util.CryptoUtil;
import com.checklersplusplus.server.util.VerificationCodeUtil;

import jakarta.validation.Valid;

@Service
@Transactional
public class AccountService {

	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	public Account findByUsername(String username) {
		Optional<AccountModel> accountModel = accountRepository.getByUsername(username);
		
		if (accountModel.isEmpty()) {
			return null;
		}
		
		return new Account(accountModel.get().getAccountId(), accountModel.get().getUsername());
	}
	
	public Account findByEmail(String email) {
		Optional<AccountModel> accountModel = accountRepository.getByEmail(email);
		
		if (accountModel.isEmpty()) {
			return null;
		}
		
		return new Account(accountModel.get().getAccountId(), accountModel.get().getUsername());
	}

	public NewAccount createAccount(@Valid CreateAccount createAccount) throws Exception {
		AccountModel accountModel = new AccountModel();
		accountModel.setUsername(createAccount.getUsername());
		accountModel.setEmail(createAccount.getEmail());
		accountModel.setPassword(CryptoUtil.encryptPassword(createAccount.getPassword()));
		accountModel.setCreated(LocalDateTime.now());
		accountRepository.save(accountModel);
		VerifyAccountModel verifyAccountModel = new VerifyAccountModel();
		verifyAccountModel.setAccountId(accountModel.getAccountId());
		verifyAccountModel.setCreated(Timestamp.valueOf(LocalDateTime.now()));
		String verificationCode = VerificationCodeUtil.generateVerificationCode();
		verifyAccountModel.setVerificationCode(verificationCode);
		verifyAccountModel.setActive(true);
		verifyAccountRepository.save(verifyAccountModel);
		NewAccount newAccount = new NewAccount(accountModel.getAccountId(), verificationCode);
		return newAccount;
	}

	public Session login(String username, String password) throws CheckersPlusPlusServerException, AccountNotVerifiedException {
		Optional<AccountModel> account = accountRepository.findByUsernameAndPassword(username, CryptoUtil.encryptPassword(password));
		
		if (account.isEmpty()) {
			throw new CheckersPlusPlusServerException("Failed to login. Account not found.");
		}
		
		if (account.get().getVerified() == null) {
			throw new AccountNotVerifiedException("Account not verified.");
		}
		
		sessionRepository.inactiveExistingSessions(account.get().getAccountId());
		SessionModel sessionModel = new SessionModel();
		sessionModel.setLastModified(Timestamp.valueOf(LocalDateTime.now()));
		sessionModel.setAccountId(account.get().getAccountId());
		sessionModel.setActive(true);
		sessionRepository.save(sessionModel);
		Session session = new Session();
		session.setSessionId(sessionModel.getSessionId());
		
		Optional<GameModel> currentGame = gameRepository.getByAccountId(account.get().getAccountId());
		if (currentGame.isPresent()) {
			session.setGameId(currentGame.get().getGameId());
		}
		
		session.setMessage("Login successful.");
		return session;
	}
	
	public void resetPassword(String username, String verificationCode, String password) throws CheckersPlusPlusServerException {
		Optional<AccountModel> account = accountRepository.getByUsername(username);
		
		if (account.isEmpty()) {
			throw new UsernameNotFoundException();
		}
		
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getLatestByAccountId(account.get().getAccountId());
		
		if (verifyAccount.isEmpty() || !verificationCode.equals(verifyAccount.get().getVerificationCode())) {
			throw new InvalidVerificationCodeException();
		}
		
		account.get().setPassword(CryptoUtil.encryptPassword(password));
		accountRepository.save(account.get());
	}
}
