package com.checkersplusplus.service;

import com.checkersplusplus.service.models.Game;

public interface GameService {

	boolean hasActiveGame(String token);

	Game createGame(String token);

	Game getActiveGame(String token);

}
