package com.checkersplusplus.service;

import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;

public interface GameService {

	boolean hasActiveGame(String token) throws Exception;

	Game createGame(String token) throws Exception;

	Game getActiveGame(String token);

	Game joinGame(String tokenId, String gameId) throws Exception;

	OpenGames getOpenGames(String tokenId) throws Exception;

}
