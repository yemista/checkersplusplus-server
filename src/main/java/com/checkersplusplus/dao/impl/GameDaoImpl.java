package com.checkersplusplus.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.GameDao;
import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.dao.models.ActiveGameModel;
import com.checkersplusplus.dao.models.GameModel;
import com.checkersplusplus.engine.enums.Color;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.ActiveGame;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Session;

@Repository
@Transactional
@Component
public class GameDaoImpl implements GameDao {

	private static final Logger logger = Logger.getLogger(GameDaoImpl.class);
	
	@Autowired
	private SessionDao sessionDao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public Game getActiveGame(String token) {
		Session session = sessionDao.getSessionByTokenId(token);
		logger.debug("fetching active game for userId: " + session.getUserId());
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ActiveGameModel> query = builder.createQuery(ActiveGameModel.class);
		Root<ActiveGameModel> root = query.from(ActiveGameModel.class);
		query.select(root).where(builder.equal(root.get("userId"), session.getUserId()));
		Query<ActiveGameModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<ActiveGameModel> listResult = q.getResultList();
		ActiveGameModel activeGameModel = listResult.isEmpty() ? null : listResult.get(0);
		
		if (activeGameModel == null) {
			logger.debug("no active game found for userId: " + session.getUserId());
		} else {
			logger.debug("found active game for userId: " + session.getUserId());
		}
		
		return activeGameModel == null ? null : getGameById(activeGameModel.getGameId());
	}

	private ActiveGame insertIntoActiveGames(String userId) {
		ActiveGameModel activeGameModel = new ActiveGameModel();
		activeGameModel.setUserId(userId);
		activeGameModel.setGameId(UUID.randomUUID().toString());
		sessionFactory.getCurrentSession().persist(activeGameModel);
		return new ActiveGame(activeGameModel.getUserId(), activeGameModel.getGameId());
	}

	@Override
	public Game initializeGame(String token) {
		Session session = sessionDao.getSessionByTokenId(token);
		logger.debug("initializing game for userId: " + session.getUserId());
		ActiveGame activeGame = insertIntoActiveGames(session.getUserId());
		GameModel gameModel = new GameModel();
		gameModel.setBlackId(session.getUserId());
		gameModel.setCreated(new Date());
		gameModel.setId(activeGame.getGameId());
		com.checkersplusplus.engine.Game gameEngine = new com.checkersplusplus.engine.Game();
		gameModel.setState(gameEngine.getGameState());
		sessionFactory.getCurrentSession().persist(gameModel);
		logger.debug("successfully initialized game for userId: " + session.getUserId());
		return new Game(gameModel.getId(), gameModel.getState(), GameStatus.PENDING.toString(), gameModel.getBlackId());
	}
	
	@Override
	public Game getGameById(String gameId) {
		logger.debug("fetching game by id: " + gameId);
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<GameModel> query = builder.createQuery(GameModel.class);
		Root<GameModel> root = query.from(GameModel.class);
		query.select(root).where(builder.equal(root.get("id"), gameId));
		Query<GameModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<GameModel> listResult = q.getResultList();
		GameModel gameModel = listResult.isEmpty() ? null : listResult.get(0);
		
		if (gameModel == null) {
			logger.debug("failed to find game by id: " + gameId);
		} else {
			logger.debug("found game by id: " + gameId);
		}
		
		return gameModel == null ? null : new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), getNextToAct(gameModel));
	}

	private String getNextToAct(GameModel gameModel) {
		String state = gameModel.getState();
		String[] parts = state.split("\\|");
		
		if (parts.length < 2) {
			logger.debug("getNextToAct(GameModel) parsed an invalid game state: " + state);
			return gameModel.getBlackId();
		}
		
		char[] chars = parts[0].toCharArray();
		
		if (chars[1] == Color.BLACK.getSymbol()) {
			return gameModel.getBlackId();
		}
		
		return gameModel.getRedId();
	}

	private String getGameStatus(GameModel gameModel) {
		if (gameModel.getRedId() == null) {
			return GameStatus.PENDING.toString();
		}
		
		if (gameModel.getWinnerId() != null) {
			return GameStatus.COMPLETE.toString();
		}
		
		if (gameModel.getForfeitId() != null) {
			return GameStatus.ABORTED.toString();
		}
		
		return GameStatus.RUNNING.toString();
	}

}
