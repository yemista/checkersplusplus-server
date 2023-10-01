package com.checklersplusplus.server.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.SessionModel;

public interface SessionRepository extends JpaRepository<SessionModel, UUID> {

	@Modifying
	@Query("update Session set active = false where accountId = ?1")
	public void inactiveExistingSessions(UUID accountID);
}
