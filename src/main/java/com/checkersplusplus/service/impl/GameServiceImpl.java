package com.checkersplusplus.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.checkersplusplus.dao.GameDao;
import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.exceptions.SessionExpiredException;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;
import com.checkersplusplus.service.models.Session;

@Component
public class GameServiceImpl implements GameService {

	private static final Logger logger = Logger.getLogger(GameServiceImpl.class);
		
	@Autowired
	private SessionDao sessionDao;
	
	@Autowired
	private GameDao gameDao;
	
	@Override
	public boolean hasActiveGame(String token) throws Exception {
		if (getSession(token) == null) {
			logger.debug("hasActiveGame(String) failed due to inactive session");
			throw new SessionExpiredException();
		}
		
		Game activeGame = gameDao.getActiveGame(token);
		logger.debug("Found game " + (activeGame == null ? "none" : activeGame.getId()) + " for session " + token);
		return activeGame != null;
	}

	@Override
	public Game createGame(String token) throws Exception {
		if (getSession(token) == null) {
			logger.debug("createGame(String) failed due to inactive session");
			throw new SessionExpiredException();
		}
		
		return gameDao.initializeGame(token);
	}

	@Override
	public Game getActiveGame(String token) throws Exception {
		Session session = getSession(token);
		
		if (session == null) {
			logger.debug("getActiveGame(String) failed due to inactive session");
			throw new SessionExpiredException();
		}
		
		Game game = gameDao.getActiveGame(token);
		return game;
	}
	
	@Override
	public Game joinGame(String tokenId, String gameId) throws Exception {
		if (getSession(tokenId) == null) {
			logger.debug("createGame(String) failed due to inactive session");
			throw new SessionExpiredException();
		}
		
		Session session = sessionDao.getSessionByTokenId(tokenId);
		return gameDao.joinGame(session.getUserId(), gameId);
	}

	@Override
	public OpenGames getOpenGames(String tokenId) throws Exception {
		if (getSession(tokenId) == null) {
			logger.debug("createGame(String) failed due to inactive session");
			throw new SessionExpiredException();
		}
		
		return gameDao.getOpenGames();
	}
	
	private Session getSession(String token) {
		logger.debug("Looking up session for token: " + token);
		Session session = sessionDao.getSessionByTokenId(token);
		
		if (session == null) {
			logger.debug("Unable to find session for token: " + token);
		}
		
		return session;
	}
}
