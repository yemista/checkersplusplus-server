package com.checkersplusplus.dao;

import com.checkersplusplus.dao.models.UserModel;

public interface UsersDao {
	
	UserModel getUserByEmail(String email);
	
	boolean isAliasInUse(String alias);
	
	boolean isEmailInUse(String email);
	
	void createUser(String email, String password, String alias);
}
