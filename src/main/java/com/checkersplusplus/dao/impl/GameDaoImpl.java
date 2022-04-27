package com.checkersplusplus.dao.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
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
import com.checkersplusplus.exceptions.CannotJoinGameException;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.ActiveGame;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;
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
	public Game joinGame(String userId, String gameId) throws Exception {
		insertIntoActiveGames(userId, gameId);
		GameModel gameModel = sessionFactory.getCurrentSession().get(GameModel.class, gameId, LockMode.PESSIMISTIC_WRITE);
		
		if (StringUtils.isNotBlank(gameModel.getBlackId())) {
			throw new CannotJoinGameException();
		}
		
		gameModel.setBlackId(userId);
		sessionFactory.getCurrentSession().merge(gameModel);
		return new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<Game> getActiveGames() {
		logger.debug("fetching all active games");
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ActiveGameModel> query = builder.createQuery(ActiveGameModel.class);
		Root<ActiveGameModel> root = query.from(ActiveGameModel.class);
		query.select(root);
		Query<ActiveGameModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<ActiveGameModel> listResult = q.getResultList();
		List<String> gameIds = listResult.stream()
						 			 .map(agm -> agm.getGameId())
						 			 .collect(Collectors.toList());
		
		if (CollectionUtils.isEmpty(gameIds)) {
			return Collections.emptyList();
		}
		
		CriteriaBuilder gameCriteriaBuilder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<GameModel> gameCriteriaQuery = gameCriteriaBuilder.createQuery(GameModel.class);
		Root<GameModel> gameRoot = gameCriteriaQuery.from(GameModel.class);
		gameCriteriaQuery.select(gameRoot).where(gameRoot.get("id").in(gameIds));
		Query<GameModel> gameQuery = sessionFactory.getCurrentSession().createQuery(gameCriteriaQuery);
		List<GameModel> gameListResult = gameQuery.getResultList();
		return gameListResult.stream()
							 .map(gameModel -> new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId()))
							 .collect(Collectors.toList());
	}
	
	@Override
	@Transactional(readOnly = true)
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

	@Override
	@Transactional
	public Game initializeGame(String token) {
		Session session = sessionDao.getSessionByTokenId(token);
		logger.debug("initializing game for userId: " + session.getUserId());
		ActiveGame activeGame = insertIntoActiveGames(session.getUserId());
		GameModel gameModel = new GameModel();
		gameModel.setRedId(session.getUserId());
		gameModel.setCreated(new Date());
		gameModel.setId(activeGame.getGameId());
		com.checkersplusplus.engine.Game gameEngine = new com.checkersplusplus.engine.Game();
		gameModel.setState(gameEngine.getGameState());
		sessionFactory.getCurrentSession().persist(gameModel);
		logger.debug("successfully initialized game for userId: " + session.getUserId());
		return new Game(gameModel.getId(), gameModel.getState(), GameStatus.PENDING, gameModel.getRedId(), null);
	}
	
	@Override
	@Transactional(readOnly = true)
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
		
		return gameModel == null ? null : new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId());
	}

	private GameStatus getGameStatus(GameModel gameModel) {
		if (gameModel.getBlackId() == null) {
			return GameStatus.PENDING;
		}
		
		if (gameModel.getWinnerId() != null) {
			return GameStatus.COMPLETE;
		}
		
		if (gameModel.getForfeitId() != null) {
			return GameStatus.ABORTED;
		}
		
		if (gameModel.getBlackId() != null && gameModel.getRedId() != null) {
			return GameStatus.RUNNING;
		}
		
		return GameStatus.CANCELED;
	}

	private ActiveGame insertIntoActiveGames(String userId) {
		ActiveGameModel activeGameModel = new ActiveGameModel();
		activeGameModel.setUserId(userId);
		activeGameModel.setGameId(UUID.randomUUID().toString());
		sessionFactory.getCurrentSession().persist(activeGameModel);
		return new ActiveGame(activeGameModel.getUserId(), activeGameModel.getGameId());
	}
	
	private ActiveGame insertIntoActiveGames(String userId, String gameId) {
		ActiveGameModel activeGameModel = new ActiveGameModel();
		activeGameModel.setUserId(userId);
		activeGameModel.setGameId(gameId);
		sessionFactory.getCurrentSession().persist(activeGameModel);
		return new ActiveGame(activeGameModel.getUserId(), activeGameModel.getGameId());
	}

	@Override
	public void forfeitGame(String id, String userId) {
		logger.debug(String.format("Forfeiting game %s by %s", id, userId));
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaUpdate<GameModel> update = builder.createCriteriaUpdate(GameModel.class);
		Root root = update.from(GameModel.class);
        update.set("forfeitId", userId);
        update.where(builder.equal(root.get("id"), id));
        int numSessionsDeactivated = sessionFactory.getCurrentSession().createQuery(update).executeUpdate();
        logger.debug(String.format("Forfeited game %s by %s", id, userId));
	}

	@Override
	@Transactional(readOnly = true)
	public OpenGames getOpenGames() throws Exception {
		logger.debug("fetching all open games");
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ActiveGameModel> query = builder.createQuery(ActiveGameModel.class);
		Root<ActiveGameModel> root = query.from(ActiveGameModel.class);
		query.select(root);
		Query<ActiveGameModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<ActiveGameModel> listResult = q.getResultList();
		List<String> gameIds = listResult.stream()
						 			 .map(agm -> agm.getGameId())
						 			 .collect(Collectors.toList());
		
		if (CollectionUtils.isEmpty(gameIds)) {
			return new OpenGames(Collections.emptyList());
		}
		
		CriteriaBuilder gameCriteriaBuilder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<GameModel> gameCriteriaQuery = gameCriteriaBuilder.createQuery(GameModel.class);
		Root<GameModel> gameRoot = gameCriteriaQuery.from(GameModel.class);
		gameCriteriaQuery.select(gameRoot).where(builder.and(gameRoot.get("id").in(gameIds), gameRoot.get("blackId").isNull()));
		Query<GameModel> gameQuery = sessionFactory.getCurrentSession().createQuery(gameCriteriaQuery);
		List<GameModel> gameListResult = gameQuery.getResultList();
		return new OpenGames(gameListResult.stream()
							 .map(gameModel -> new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId()))
							 .collect(Collectors.toList()));
	}
}
