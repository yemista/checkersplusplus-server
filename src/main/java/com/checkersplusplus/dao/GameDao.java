package com.checkersplusplus.dao;

import java.util.List;

import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;

public interface GameDao {

	Game getActiveGame(String token);

	Game initializeGame(String token);

	Game getGameById(String gameId);
	
	List<Game> getActiveGames();

	void forfeitGame(String id, String userId);

	Game joinGame(String userId, String gameId) throws Exception;

	OpenGames getOpenGames() throws Exception;
}
