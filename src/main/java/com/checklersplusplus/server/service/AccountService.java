package com.checklersplusplus.server.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameEventRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.LastMoveSentRepository;
import com.checklersplusplus.server.dao.RatingRepository;
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
import com.checklersplusplus.server.model.LastMoveSentModel;
import com.checklersplusplus.server.model.RatingModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.model.VerifyAccountModel;
import com.checklersplusplus.server.util.CryptoUtil;
import com.checklersplusplus.server.util.VerificationCodeUtil;

@Service
@Transactional
public class AccountService {
	
	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private LastMoveSentRepository lastMoveSentRepository;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	public Account findByUsername(String username) {
		Optional<AccountModel> accountModel = accountRepository.getByUsernameIgnoreCase(username);
		
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

	@Transactional
	public NewAccount createAccount(CreateAccount createAccount, boolean needsVerification) throws CheckersPlusPlusServerException, Exception {
		AccountModel accountModel = new AccountModel();
		accountModel.setUsername(createAccount.getUsername().trim());
		accountModel.setEmail(createAccount.getEmail().trim());
		accountModel.setPassword(CryptoUtil.encryptPassword(createAccount.getPassword().trim()));
		accountModel.setCreated(LocalDateTime.now());
		accountModel.setBanned(false);
		accountModel.setBot(false);
		accountModel.setTutorial(true);
		accountRepository.save(accountModel);
		
		VerifyAccountModel verifyAccountModel = new VerifyAccountModel();
		verifyAccountModel.setAccountId(accountModel.getAccountId());
		verifyAccountModel.setCreated(LocalDateTime.now());
		String verificationCode = VerificationCodeUtil.generateVerificationCode();
		verifyAccountModel.setVerificationCode(verificationCode);
		verifyAccountModel.setActive(needsVerification);
		verifyAccountRepository.save(verifyAccountModel);
		NewAccount newAccount = new NewAccount(accountModel.getAccountId(), verificationCode);

		RatingModel ratingModel = new RatingModel();
		ratingModel.setAccountId(accountModel.getAccountId());
		ratingModel.setRating(800);
		ratingRepository.save(ratingModel);

		logger.error(String.format("Size: %d Encrypted: %s", CryptoUtil.encryptPassword(createAccount.getPassword()).length(), CryptoUtil.encryptPassword(createAccount.getPassword())));
		logger.debug(String.format("New account created: %s", accountModel.getAccountId().toString()));
		return newAccount;
	}

	@Transactional
	public Session login(String username, String password) throws CheckersPlusPlusServerException {
		Optional<AccountModel> account = accountRepository.findByUsernameAndPassword(username.trim(), CryptoUtil.encryptPassword(password.trim()));
		
		if (account.isEmpty()) {
			Optional<AccountModel> accountByUsername = accountRepository.findByUsername(username.trim());
			
			if (accountByUsername.isPresent()) {
				throw new CheckersPlusPlusServerException(String.format("Failed to login. You did not enter the correct password."));
			} else {
				throw new CheckersPlusPlusServerException(String.format("Failed to login. Account %s not found.", username));
			}
		}
		
		if (account.get().getVerified() == null) {
			throw new AccountNotVerifiedException();
		}
		
		if (account.get().isBanned()) {
			throw new CheckersPlusPlusServerException("This account has been banned. Please email admin@checkersplusplus.com to find out why.");
		}
		
		sessionRepository.inactiveExistingSessions(account.get().getAccountId());
		SessionModel sessionModel = new SessionModel();
		sessionModel.setLastModified(LocalDateTime.now());
		sessionModel.setAccountId(account.get().getAccountId());
		sessionModel.setActive(true);
		sessionRepository.save(sessionModel);
		Session session = new Session();
		session.setSessionId(sessionModel.getSessionId());
		session.setAccountId(account.get().getAccountId());
		session.setTutorial(String.valueOf(account.get().isTutorial()));
		
		Optional<GameModel> currentGame = gameRepository.getActiveGameByAccountId(account.get().getAccountId());
		
		if (currentGame.isPresent()) {
			session.setGameId(currentGame.get().getGameId());
			addLastMoveSentIfNecessary(currentGame.get(), currentGame.get().getGameId(), account.get().getAccountId());
		} else {
			gameEventRepository.inactivateEventsForRecipient(account.get().getAccountId());
		}

		session.setMessage("Login successful.");
		logger.info(String.format("Login successful for %s", account.get().getUsername()));
		return session;
	}
	
	@Transactional
	public Session ssoLogin(String email) throws Exception {
		Optional<AccountModel> account = accountRepository.getByEmail(email.trim());
		UUID accountId = null;
		boolean tutorial = false;
		Session session = new Session();
				
		if (account.isEmpty()) {
			String username = email.substring(0, email.indexOf('@'));
			account = accountRepository.getByUsernameIgnoreCase(username);
			int counter = 1;
			String finalUsername = username;
			
			while (account.isPresent()) {
				finalUsername = username + counter;
				account = accountRepository.getByUsernameIgnoreCase(finalUsername);
				counter++;
			}
			
			
			CreateAccount createAccount = new CreateAccount();
			createAccount.setEmail(email);
			createAccount.setPassword(CryptoUtil.encryptPassword("DEFAULT_PASSWORD"));
			createAccount.setUsername(finalUsername);
			
			AccountModel accountModel = new AccountModel();
			accountModel.setUsername(createAccount.getUsername().trim());
			accountModel.setEmail(createAccount.getEmail().trim());
			accountModel.setPassword(CryptoUtil.encryptPassword(createAccount.getPassword().trim()));
			accountModel.setCreated(LocalDateTime.now());
			accountModel.setBanned(false);
			accountModel.setBot(false);
			accountModel.setTutorial(true);
			accountRepository.saveAndFlush(accountModel);
			
			VerifyAccountModel verifyAccountModel = new VerifyAccountModel();
			verifyAccountModel.setAccountId(accountModel.getAccountId());
			verifyAccountModel.setCreated(LocalDateTime.now());
			String verificationCode = VerificationCodeUtil.generateVerificationCode();
			verifyAccountModel.setVerificationCode(verificationCode);
			verifyAccountModel.setActive(false);
			verifyAccountRepository.saveAndFlush(verifyAccountModel);
			NewAccount newAccount = new NewAccount(accountModel.getAccountId(), verificationCode);

			RatingModel ratingModel = new RatingModel();
			ratingModel.setAccountId(accountModel.getAccountId());
			ratingModel.setRating(800);
			ratingRepository.saveAndFlush(ratingModel);
			
			accountId = newAccount.getAccountId();
			tutorial = true;
		} else {
			accountId = account.get().getAccountId();
			tutorial = account.get().isTutorial();
			
			if (account.get().isBanned()) {
				throw new CheckersPlusPlusServerException("This account has been banned. Please email admin@checkersplusplus.com to find out why.");
			}
			
			sessionRepository.inactiveExistingSessions(accountId);
			Optional<GameModel> currentGame = gameRepository.getActiveGameByAccountId(account.get().getAccountId());
			
			if (currentGame.isPresent()) {
				session.setGameId(currentGame.get().getGameId());
				addLastMoveSentIfNecessary(currentGame.get(), currentGame.get().getGameId(), account.get().getAccountId());
			} else {
				gameEventRepository.inactivateEventsForRecipient(account.get().getAccountId());
			}
		}
		try {
			SessionModel sessionModel = new SessionModel();
			sessionModel.setLastModified(LocalDateTime.now());
			sessionModel.setAccountId(accountId);
			sessionModel.setActive(true);
		
		
			sessionRepository.saveAndFlush(sessionModel);
		
			session.setSessionId(sessionModel.getSessionId());
			session.setAccountId(accountId);
			session.setTutorial(String.valueOf(tutorial));
		} catch (Throwable e) {
			logger.error(e.toString());
		}

		session.setMessage("Login successful.");
		logger.info(String.format("SSO Login successful for accountId: %s", accountId.toString()));
		return session;
	}

	private void addLastMoveSentIfNecessary(GameModel game, UUID gameId, UUID accountId) {
		Optional<LastMoveSentModel> lastMove = lastMoveSentRepository.findFirstByAccountIdAndGameIdOrderByLastMoveSentDesc(accountId, gameId);
		
		if ((lastMove.isEmpty() && game.getCurrentMoveNumber() > 0)
				|| (lastMove.isPresent() && lastMove.get().getLastMoveSent() != game.getCurrentMoveNumber())) {
			LastMoveSentModel latestMove = new LastMoveSentModel();
			latestMove.setAccountId(accountId);
			latestMove.setCreated(LocalDateTime.now());
			latestMove.setGameId(gameId);
			latestMove.setLastMoveSent(game.getCurrentMoveNumber());
			lastMoveSentRepository.save(latestMove);
		}		
	}

	@Transactional
	public void resetPassword(String username, String verificationCode, String password) throws CheckersPlusPlusServerException {
		Optional<AccountModel> account = accountRepository.getByUsernameIgnoreCase(username.trim());
		
		if (account.isEmpty()) {
			throw new UsernameNotFoundException();
		}
		
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getActiveByAccountId(account.get().getAccountId());
		
		if (verifyAccount.isEmpty() || !verificationCode.equals(verifyAccount.get().getVerificationCode())) {
			throw new InvalidVerificationCodeException();
		}
		
		account.get().setPassword(CryptoUtil.encryptPassword(password));
		accountRepository.save(account.get());
		verifyAccount.get().setActive(false);
		verifyAccountRepository.save(verifyAccount.get());
		logger.info(String.format("Password reset for %s", account.get().getUsername()));
	}
}
