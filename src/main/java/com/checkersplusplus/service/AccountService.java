package com.checkersplusplus.service;

import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.User;

public interface AccountService {

	boolean isPasswordSafe(String password);
	
	boolean isAliasUnique(String alias);
	
	boolean isEmailUnique(String email);
	
	void createAccount(String email, String password, String alias);
	
	User getAccount(String email);

	boolean isAliasValid(String alias);

	Login login(String email);

	boolean isLoginValid(String email, String password);
	
	boolean isEmailValid(String email);
}
