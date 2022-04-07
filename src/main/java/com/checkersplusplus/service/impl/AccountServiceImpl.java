package com.checkersplusplus.service.impl;

import org.apache.commons.validator.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.checkersplusplus.controllers.inputs.LoginInput;
import com.checkersplusplus.crypto.PasswordCryptoUtil;
import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.dao.UsersDao;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.models.User;

@Component
public class AccountServiceImpl implements AccountService {
	
	@Autowired
	private UsersDao usersDao;
	
	@Autowired
	private SessionDao sessionDao;
	
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
		return !usersDao.isAliasInUse(alias);
	}

	public boolean isEmailUnique(String email) {
		return !usersDao.isEmailInUse(email);
	}

	public void createAccount(String email, String password, String alias) {
		usersDao.createUser(email, password, alias);
	}

	@Override
	public User getAccount(String email) {
		return usersDao.getUserByEmail(email);
	}
	
	@Override
	public boolean isEmailValid(String email) {
		return EmailValidator.getInstance().isValid(email);
	}

	@Override
	public boolean isAliasValid(String alias) {
		return alias != null && alias.length() >= 3 && alias.length() < 15;
	}

	@Override
	public String login(LoginInput payload) {
		User user = usersDao.getUserByEmail(payload.getEmail());
		return sessionDao.createUserSession(user.getId());
	}

	@Override
	public boolean isLoginValid(LoginInput payload) {
		User user = usersDao.getUserByEmail(payload.getEmail());
		
		if (user == null) {
			return false;
		}
		
		return PasswordCryptoUtil.encryptPasswordForDatabase(payload.getPassword()).equals(user.getPassword());
	}
}

