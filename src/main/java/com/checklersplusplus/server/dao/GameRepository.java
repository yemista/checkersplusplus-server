package com.checklersplusplus.server.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.GameModel;

public interface GameRepository extends JpaRepository<GameModel, UUID> {

	Optional<GameModel> getByGameId(UUID gameId);
	
	@Query("SELECT g FROM GameModel g WHERE (g.redId = ?1 OR g.blackId = ?1) AND g.active=true") 
	Optional<GameModel> getByAccountId(UUID accountId);
	
	@Query("SELECT g FROM GameModel g WHERE (g.redId IS NULL OR g.blackId IS NULL)") 
	List<GameModel> getOpenGames();
}
