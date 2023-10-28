package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checklersplusplus.server.model.RatingModel;

public interface RatingRepository extends JpaRepository<RatingModel, UUID> {

	public Optional<RatingModel> findByAccountId(UUID accountId);
}
