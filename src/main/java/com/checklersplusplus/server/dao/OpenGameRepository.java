package com.checklersplusplus.server.dao;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.checklersplusplus.server.model.GameModel;

public interface OpenGameRepository extends PagingAndSortingRepository<GameModel, UUID> {
	
	Page<GameModel> findByCreatorRatingBetweenAndActiveTrue(Integer lowRating, Integer highRating, Pageable pageable);
}
