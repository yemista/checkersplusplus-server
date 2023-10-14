package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.SessionModel;

public interface SessionRepository extends JpaRepository<SessionModel, UUID> {

	@Modifying
	@Query("UPDATE SessionModel SET active = false WHERE accountId = ?1")
	public void inactiveExistingSessions(UUID accountID);
	
	@Query("SELECT s FROM SessionModel s WHERE s.sessionId = ?1 AND active=true")
	public Optional<SessionModel> getActiveBySessionId(UUID sessionId);
	
	@Query("SELECT s FROM SessionModel s WHERE s.accountId = ?1 AND active=true")
	public Optional<SessionModel> getActiveByAccountId(UUID accountId);
}
