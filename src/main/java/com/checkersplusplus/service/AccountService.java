package com.checkersplusplus.service;

import java.util.Date;
import java.util.UUID;

import org.apache.commons.validator.EmailValidator;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.crypto.PasswordCryptoUtil;
import com.checkersplusplus.dao.UserRepository;
import com.checkersplusplus.dao.models.UserModel;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.User;

@Service
@Transactional
public class AccountService {
	
	private static final Logger logger = Logger.getLogger(AccountService.class);
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private SessionService sessionService;
	
	public boolean isPasswordSafe(String password) {
		return password != null && password.length() >= 8 && password.length() <= 20 && passwordContainsOnlyLettersAndDigits(password);
	}

	private boolean passwordContainsOnlyLettersAndDigits(String password) {
		char[] chars = password.toCharArray();
		
		for (int charIndex = 0; charIndex < chars.length; ++charIndex) {
			if (!charIsLetterOrDigit(chars[charIndex])) {
				return false;				
			}
		}
		
		return true;
	}

	private boolean charIsLetterOrDigit(char c) {
		return (c >= '0' && c <= '9')
				|| (c >= 'A' && c <= 'Z')
				|| (c >= 'a' && c <= 'z');
	}

	public boolean isAliasUnique(String alias) {
		return userRepository.findByAlias(alias) == null;
	}

	public boolean isEmailUnique(String email) {
		return userRepository.findByEmail(email) == null;
	}

	public void createAccount(String email, String password, String alias) {
		UserModel user = new UserModel();
		user.setId(UUID.randomUUID().toString());
		user.setEmail(email);
		user.setPassword(PasswordCryptoUtil.encryptPasswordForDatabase(password));
		user.setAlias(alias);
		user.setCreateDate(new Date());
		userRepository.save(user);
		logger.debug("Created account for email: " + email);
		
	}

	public User getAccount(String email) {
		UserModel userModel = userRepository.findByEmail(email);
		
		if (userModel == null) {
			return null;
		}
		
		return new User(userModel.getId(), userModel.getEmail(), userModel.getPassword(), userModel.getAlias());
	}
	
	public boolean isEmailValid(String email) {
		return EmailValidator.getInstance().isValid(email);
	}

	public boolean isAliasValid(String alias) {
		return alias != null && alias.length() >= 3 && alias.length() < 15;
	}

	public Login login(String email) {
		User user = getAccount(email);
		
		if (user == null) {
			return null;
		}
		
		String sessionId = sessionService.createUserSession(user.getId());
		return new Login(user.getId(), sessionId);
	}

	public boolean isLoginValid(String email, String password) {
		User user = getAccount(email);
		
		if (user == null) {
			return false;
		}
		
		return PasswordCryptoUtil.encryptPasswordForDatabase(password).equals(user.getPassword());
	}
}
