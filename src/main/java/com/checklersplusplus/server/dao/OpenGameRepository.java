package com.checklersplusplus.server.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.checklersplusplus.server.model.GameModel;

public interface OpenGameRepository extends PagingAndSortingRepository<GameModel, UUID> {
	
	Page<GameModel> findByCreatorRatingBetweenAndActiveTrueAndInProgressFalse(Integer lowRating, Integer highRating, Pageable pageable);
	
	@Query( "SELECT g FROM GameModel g WHERE g.active=true AND g.inProgress=false AND (blackId in :accountIds OR redId in :accountIds)" )
	Page<GameModel> findByUserId(List<UUID> accountIds, Pageable pageable);
}
