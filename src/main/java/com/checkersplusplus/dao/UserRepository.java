package com.checkersplusplus.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.checkersplusplus.dao.models.UserModel;

public interface UserRepository extends CrudRepository<UserModel, String> {

	UserModel findByEmail(String email);
	
	UserModel findByAlias(String alias);
	
	@Modifying
	@Query("UPDATE UserModel u SET u.verified = 1 WHERE u.email = :email")
	public void verifyAccount(@Param("email") String email);
}
