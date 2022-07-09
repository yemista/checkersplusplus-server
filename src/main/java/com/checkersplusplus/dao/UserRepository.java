package com.checkersplusplus.dao;

import org.springframework.data.repository.CrudRepository;

import com.checkersplusplus.dao.models.UserModel;

public interface UserRepository extends CrudRepository<UserModel, String> {

	UserModel findByEmail(String email);
	
	UserModel findByAlias(String alias);
}
