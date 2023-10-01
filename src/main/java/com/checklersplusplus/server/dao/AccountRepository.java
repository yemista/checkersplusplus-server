package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checklersplusplus.server.model.AccountModel;

public interface AccountRepository extends JpaRepository<AccountModel, UUID> {

	public Optional<AccountModel> getByUsername(String username);
	
	public Optional<AccountModel> getByEmail(String email);

	public Optional<AccountModel> findByUsernameAndPassword(String username, String password);
}
