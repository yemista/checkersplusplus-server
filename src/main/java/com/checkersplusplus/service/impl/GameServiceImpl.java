package com.checkersplusplus.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.checkersplusplus.dao.GameDao;
import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.ActiveGame;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Session;

@Component
public class GameServiceImpl implements GameService {

	private static final Logger logger = Logger.getLogger(GameServiceImpl.class);
		
	@Autowired
	private SessionDao sessionDao;
	
	@Autowired
	private GameDao gameDao;
	
	@Override
	public boolean hasActiveGame(String token) {
		if (!sessionActive(token)) {
			logger.debug("hasActiveGame(String) failed due to inactive session");
			return false;
		}
		
		ActiveGame activeGame = gameDao.getActiveGameByUser(session.getUserId());
		logger.debug("Found game " + (activeGame == null ? "none" : activeGame.getGameId()) + " for session " + token);
		return activeGame != null;
	}

	@Override
	public Game createGame(String token) {
		if (!sessionActive(token)) {
			logger.debug("createGame(String) failed due to inactive session");
			return false;
		}
		
		return gameDao.initializeGame(token);
	}

	@Override
	public Game getActiveGame(String token) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean sessionActive(String token) {
		logger.debug("Looking up session for token: " + token);
		Session session = sessionDao.getSessionByTokenId(token);
		
		if (session == null) {
			logger.debug("Unable to find session for token: " + token);
			return false;
		}
		
		return true;
	}

}
