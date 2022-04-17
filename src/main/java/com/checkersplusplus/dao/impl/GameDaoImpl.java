package com.checkersplusplus.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.ActiveGame;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Session;

@Repository
@Transactional
@Component
public class GameDaoImpl implements GameDao {

	@Autowired
	private SessionDao sessionDao;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public ActiveGame getActiveGameByUser(String userId) {
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<ActiveGameModel> query = builder.createQuery(ActiveGameModel.class);
		Root<ActiveGameModel> root = query.from(ActiveGameModel.class);
		query.select(root).where(builder.equal(root.get("userId"), userId));
		Query<ActiveGameModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<ActiveGameModel> listResult = q.getResultList();
		ActiveGameModel activeGameModel = listResult.isEmpty() ? null : listResult.get(0);
		return activeGameModel == null ? null : new ActiveGame(activeGameModel.getUserId(), activeGameModel.getGameId());
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
		ActiveGame activeGame = insertIntoActiveGames(session.getUserId());
		GameModel gameModel = new GameModel();
		gameModel.setBlackId(session.getUserId());
		gameModel.setCreated(new Date());
		gameModel.setId(UUID.randomUUID().toString());
		com.checkersplusplus.engine.Game gameEngine = new com.checkersplusplus.engine.Game();
		gameModel.setState(gameEngine.getGameState());
		sessionFactory.getCurrentSession().persist(gameModel);
		return new Game(gameModel.getId(), gameModel.getState(), GameStatus.PENDING, gameModel.getBlackId());
	}
	
	@Override
	public Game getGameById(String gameId) {
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<GameModel> query = builder.createQuery(GameModel.class);
		Root<GameModel> root = query.from(GameModel.class);
		query.select(root).where(builder.equal(root.get("id"), gameId));
		Query<GameModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<GameModel> listResult = q.getResultList();
		GameModel gameModel = listResult.isEmpty() ? null : listResult.get(0);
		return gameModel == null ? null : new Game(gameModel.getId());
	}

}
