package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.GameEventModel;

public interface GameEventRepository extends JpaRepository<GameEventModel, UUID> {

	@Query("SELECT g FROM GameEventModel g WHERE eventRecipientAccountId = ?1 AND gameId = ?2 AND active = true")
	Optional<GameEventModel> findActiveEventForAccountIdAndGameId(UUID accountId, UUID gameId);
	
	@Query("SELECT g FROM GameEventModel g WHERE eventRecipientAccountId = ?1 AND active = true")
	Optional<GameEventModel> findActiveEventForAccountId(UUID accountId);
	
	@Modifying
	@Query("UPDATE GameEventModel g SET active = false WHERE eventRecipientAccountId = ?1")
	void inactivateEventsForRecipient(UUID accountId);

}
