package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.VerifyAccountModel;

public interface VerifyAccountRepository extends JpaRepository<VerifyAccountModel, UUID> {

	public Optional<VerifyAccountModel> findByAccountIdAndVerificationCode(UUID accountId, String verificationCode);
	
	@Modifying
	@Query("UPDATE VerifyAccountModel SET active=false WHERE accountId = ?1")
	public void inactivateForAccountId(UUID accountId);
	
	@Query("SELECT v FROM VerifyAccountModel v WHERE v.accountId = ?1 AND active=true")
	public Optional<VerifyAccountModel> getByAccountId(UUID accountId);
	
}
