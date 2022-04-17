package com.checkersplusplus.dao;

import com.checkersplusplus.service.models.ActiveGame;
import com.checkersplusplus.service.models.Game;

public interface GameDao {

	ActiveGame getActiveGameByUser(String userId);

	Game initializeGame(String token);

	Game getGameById(String gameId);

}
