package com.checkersplusplus.dao;

import com.checkersplusplus.service.models.Game;

public interface GameDao {

	Game getActiveGame(String token);

	Game initializeGame(String token);

	Game getGameById(String gameId);

}
