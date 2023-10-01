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
import com.checklersplusplus.server.entities.Account;
import com.checklersplusplus.server.entities.CreateAccount;
import com.checklersplusplus.server.entities.Login;
import com.checklersplusplus.server.entities.Session;
import com.checklersplusplus.server.entities.VerifyAccount;
import com.checklersplusplus.server.exception.AccountNotVerifiedException;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
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
	public SessionRepository sessionRepository;
	
	@Autowired
	public GameRepository gameRepository;
	
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

	public void createAccount(@Valid CreateAccount createAccount) throws Exception {
		AccountModel accountModel = new AccountModel();
		accountModel.setUsername(createAccount.getUsername());
		accountModel.setEmail(createAccount.getEmail());
		accountModel.setPassword(CryptoUtil.encryptPassword(createAccount.getPassword()));
		accountModel.setCreated(Timestamp.valueOf(LocalDateTime.now()));
		accountRepository.save(accountModel);
		verifyAccountRepository.inactiveForAccountId(accountModel.getAccountId());
		VerifyAccountModel verifyAccountModel = new VerifyAccountModel();
		verifyAccountModel.setAccountId(accountModel.getAccountId());
		verifyAccountModel.setCreated(Timestamp.valueOf(LocalDateTime.now()));
		verifyAccountModel.setVerificationCode(VerificationCodeUtil.generateVerificationCode());
		verifyAccountRepository.save(verifyAccountModel);
	}
	
	public void verifyAccount(@Valid VerifyAccount verifyAccount) throws Exception {
		Optional<AccountModel> accountModelOptional = accountRepository.getByEmail(verifyAccount.getEmail());
		
		if (accountModelOptional.isEmpty()) {
			throw new Exception();
		}
		
		AccountModel accountModel = accountModelOptional.get();		
		Optional<VerifyAccountModel> verifyAccountModelOptional = verifyAccountRepository.findByAccountIdAndVerificationCode(accountModel.getAccountId(), verifyAccount.getVerificationCode());
		
		if (verifyAccountModelOptional.isEmpty() || verifyAccountModelOptional.get().getActive() == false) {
			throw new Exception();
		}
		
		VerifyAccountModel verifyAccountModel = verifyAccountModelOptional.get();
		verifyAccountModel.setActive(false);
		verifyAccountRepository.save(verifyAccountModel);
		accountModel.setVerified(Timestamp.valueOf(LocalDateTime.now()));
		accountRepository.save(accountModel);
	}

	public Session login(@Valid Login login) throws CheckersPlusPlusServerException, AccountNotVerifiedException {
		Optional<AccountModel> account = accountRepository.findByUsernameAndPassword(login.getUsername(), CryptoUtil.encryptPassword(login.getPassword()));
		
		if (account.isEmpty()) {
			throw new CheckersPlusPlusServerException("Failed to login. Account not found.");
		}
		
		if (account.get().getVerified() == null) {
			throw new AccountNotVerifiedException("Account not verified.");
		}
		
		sessionRepository.inactiveExistingSessions(account.get().getAccountId());
		SessionModel sessionModel = new SessionModel();
		sessionModel.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
		sessionModel.setAccountId(account.get().getAccountId());
		sessionModel.setActive(true);
		sessionRepository.save(sessionModel);
		Session session = new Session();
		session.setSessionId(sessionModel.getSessionId());
		
		Optional<GameModel> currentGame = gameRepository.getByAccountId(account.get().getAccountId());
		if (currentGame.isPresent()) {
			session.setGameId(currentGame.get().getGameId());
		}
		
		session.setMessage("Login Successful");
		return session;
	}

}
