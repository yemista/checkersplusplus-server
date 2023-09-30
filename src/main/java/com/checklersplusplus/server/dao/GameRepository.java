package com.checklersplusplus.server.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checklersplusplus.server.model.GameModel;

public interface GameRepository extends JpaRepository<GameModel, UUID> {

	List<GameModel> findByRedId(UUID redId);
}
