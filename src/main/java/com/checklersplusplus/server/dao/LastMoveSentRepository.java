package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checklersplusplus.server.model.LastMoveSentModel;

public interface LastMoveSentRepository extends JpaRepository<LastMoveSentModel, UUID> {
	
	public Optional<LastMoveSentModel> findFirstByAccountIdAndGameIdOrderByLastMoveSentDesc(UUID accountId, UUID gameId);
}
