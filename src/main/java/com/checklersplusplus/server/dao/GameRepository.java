package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.GameModel;

public interface GameRepository extends JpaRepository<GameModel, UUID> {

	Optional<GameModel> getByGameId(UUID gameId);
	
	@Query("SELECT g FROM Game g WHERE (g.redId = ?1 OR g.blackId = ?1) AND g.winnerId IS NULL") 
	Optional<GameModel> getByAccountId(UUID accountId);
}
