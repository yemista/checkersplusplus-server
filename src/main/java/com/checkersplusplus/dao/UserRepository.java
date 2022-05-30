package com.checkersplusplus.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.models.UserModel;

@Transactional
public interface UserRepository extends CrudRepository<UserModel, String> {

	UserModel findByEmail(String email);
	
	UserModel findByAlias(String alias);
}
