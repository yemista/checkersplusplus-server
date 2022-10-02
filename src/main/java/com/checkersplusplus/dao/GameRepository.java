package com.checkersplusplus.dao;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.models.GameModel;

@Transactional
public interface GameRepository extends JpaRepository<GameModel, String> {

	@Query("SELECT g FROM GameModel g INNER JOIN ActiveGameModel a ON g.id = a.gameId ORDER BY g.created")
	public List<GameModel> getActivesGames();
	
	@Query("SELECT g FROM SessionModel s LEFT JOIN ActiveGameModel a ON s.userId = a.userId LEFT JOIN GameModel g ON g.id = a.gameId WHERE s.token = :token AND s.active = true")
	public GameModel getActiveGameByToken(@Param("token") String token);
	
	public GameModel getById(String id);
	
	@Modifying
	@Query("UPDATE GameModel g SET g.forfeitId = :userId, g.status = 'FORFEIT' WHERE g.id = :gameId")
	public void forfeitGame(@Param("gameId") String gameId, @Param("userId") String userId);

	@Query(value = "SELECT g FROM GameModel g INNER JOIN ActiveGameModel a ON g.id = a.gameId WHERE g.blackId IS NULL",
		   countQuery = "SELECT count(*) FROM GameModel g INNER JOIN ActiveGameModel a ON g.id = a.gameId WHERE g.blackId IS NULL")
	public List<GameModel> getOpenGames(Pageable pageable);
	
	@Modifying
	@Query("UPDATE GameModel g SET g.state = :state, g.version = :version WHERE g.id = :gameId")
	public void updateGameState(@Param("gameId") String gameId, @Param("state") String state, @Param("version") int version);
}
