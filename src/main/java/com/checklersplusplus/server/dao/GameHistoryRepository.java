package com.checklersplusplus.server.dao;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.checklersplusplus.server.model.GameModel;

public interface GameHistoryRepository extends PagingAndSortingRepository<GameModel, UUID> {

	// TODO test
	@Query("SELECT g FROM GameModel g WHERE redId = ?1 OR blackId = ?1")
	Page<GameModel> findByRedIdOrBlackId(UUID accountId, PageRequest pageRequest);

}
