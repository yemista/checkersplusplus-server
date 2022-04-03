package com.checkersplusplus.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.checkersplusplus.dao.UsersDao;
import com.checkersplusplus.dao.models.UserModel;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.models.User;

@Component
public class AccountServiceImpl implements AccountService {
	
	@Autowired
	private UsersDao usersDao;
	
	public boolean isPasswordSafe(String password) {
		return password.length() >= 8 || !passwordContainsOnlyLettersAndDigits(password);
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
		UserModel userModel = usersDao.getUserByEmail(email);
		return new User(userModel.getEmail(), userModel.getPassword(), userModel.getAlias());
	}
}

