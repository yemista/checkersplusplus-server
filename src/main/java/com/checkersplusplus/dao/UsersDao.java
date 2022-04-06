package com.checkersplusplus.dao;

import com.checkersplusplus.service.models.User;

public interface UsersDao {
	
	User getUserByEmail(String email);
	
	boolean isAliasInUse(String alias);
	
	boolean isEmailInUse(String email);
	
	void createUser(String email, String password, String alias);
}
