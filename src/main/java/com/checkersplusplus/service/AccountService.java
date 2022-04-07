package com.checkersplusplus.service;

import com.checkersplusplus.controllers.inputs.LoginInput;
import com.checkersplusplus.service.models.User;

public interface AccountService {

	boolean isPasswordSafe(String password);
	
	boolean isAliasUnique(String alias);
	
	boolean isEmailUnique(String email);
	
	void createAccount(String email, String password, String alias);
	
	User getAccount(String email);

	boolean isAliasValid(String alias);

	String login(LoginInput payload);

	boolean isLoginValid(LoginInput payload);
	
	boolean isEmailValid(String email);
}
