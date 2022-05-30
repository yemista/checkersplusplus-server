package com.checkersplusplus.dao.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaDelete;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.GameDao;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;

@Repository
@Transactional
@Component
public class GameDaoImpl implements GameDao {

	private static final Logger logger = Logger.getLogger(GameDaoImpl.class);

	@Override
	public Game getActiveGame(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Game initializeGame(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Game getGameById(String gameId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Game> getActiveGames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void forfeitGame(String id, String userId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Game joinGame(String userId, String gameId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@SuppressWarnings("unused")
	private void deleteActiveGames(String...  ids) {
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
        CriteriaDelete<ActiveGameModel> delete = builder.createCriteriaDelete(ActiveGameModel.class);
        Root root = delete.from(ActiveGameModel.class);
        delete.where(builder.and(root.get("userId").in(ids)));
        sessionFactory.getCurrentSession().createQuery(delete).executeUpdate();
	}

	@Override
	public OpenGames getOpenGames() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
//	@Autowired
//	private SessionDao sessionDao;
//	
//	@Autowired
//	private SessionFactory sessionFactory;
//	
//	@Override

//	
//	@Override
//	@Transactional(readOnly = true)
//	public List<Game> getActiveGames() {
//		logger.debug("fetching all active games");
//		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
//		CriteriaQuery<ActiveGameModel> query = builder.createQuery(ActiveGameModel.class);
//		Root<ActiveGameModel> root = query.from(ActiveGameModel.class);
//		query.select(root);
//		Query<ActiveGameModel> q = sessionFactory.getCurrentSession().createQuery(query);
//		List<ActiveGameModel> listResult = q.getResultList();
//		List<String> gameIds = listResult.stream()
//						 			 .map(agm -> agm.getGameId())
//						 			 .collect(Collectors.toList());
//		
//		if (CollectionUtils.isEmpty(gameIds)) {
//			return Collections.emptyList();
//		}
//		
//		CriteriaBuilder gameCriteriaBuilder = sessionFactory.getCriteriaBuilder();
//		CriteriaQuery<GameModel> gameCriteriaQuery = gameCriteriaBuilder.createQuery(GameModel.class);
//		Root<GameModel> gameRoot = gameCriteriaQuery.from(GameModel.class);
//		gameCriteriaQuery.select(gameRoot).where(gameRoot.get("id").in(gameIds));
//		Query<GameModel> gameQuery = sessionFactory.getCurrentSession().createQuery(gameCriteriaQuery);
//		List<GameModel> gameListResult = gameQuery.getResultList();
//		return gameListResult.stream()
//							 .map(gameModel -> new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId(), gameModel.getWinnerId()))
//							 .collect(Collectors.toList());
//	}
//	
//	@Override
//	@Transactional(readOnly = true)
//	public Game getActiveGame(String token) {
//		Session session = sessionDao.getSessionByTokenId(token);
//		logger.debug("fetching active game for userId: " + session.getUserId());
//		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
//		CriteriaQuery<ActiveGameModel> query = builder.createQuery(ActiveGameModel.class);
//		Root<ActiveGameModel> root = query.from(ActiveGameModel.class);
//		query.select(root).where(builder.equal(root.get("userId"), session.getUserId()));
//		Query<ActiveGameModel> q = sessionFactory.getCurrentSession().createQuery(query);
//		List<ActiveGameModel> listResult = q.getResultList();
//		ActiveGameModel activeGameModel = listResult.isEmpty() ? null : listResult.get(0);
//		
//		if (activeGameModel == null) {
//			logger.debug("no active game found for userId: " + session.getUserId());
//		} else {
//			logger.debug("found active game for userId: " + session.getUserId());
//		}
//		
//		return activeGameModel == null ? null : getGameById(activeGameModel.getGameId());
//	}
//
//	@Override
//	@Transactional
//	public Game initializeGame(String token) {
//		Session session = sessionDao.getSessionByTokenId(token);
//		logger.debug("initializing game for userId: " + session.getUserId());
//		ActiveGame activeGame = insertIntoActiveGames(session.getUserId());
//		GameModel gameModel = new GameModel();
//		gameModel.setRedId(session.getUserId());
//		gameModel.setCreated(new Date());
//		gameModel.setId(activeGame.getGameId());
//		com.checkersplusplus.engine.Game gameEngine = new com.checkersplusplus.engine.Game();
//		gameModel.setState(gameEngine.getGameState());
//		sessionFactory.getCurrentSession().persist(gameModel);
//		logger.debug("successfully initialized game for userId: " + session.getUserId());
//		return new Game(gameModel.getId(), gameModel.getState(), GameStatus.PENDING, gameModel.getRedId(), null, null);
//	}
//	
//	@Override
//	@Transactional(readOnly = true)
//	public Game getGameById(String gameId) {
//		logger.debug("fetching game by id: " + gameId);
//		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
//		CriteriaQuery<GameModel> query = builder.createQuery(GameModel.class);
//		Root<GameModel> root = query.from(GameModel.class);
//		query.select(root).where(builder.equal(root.get("id"), gameId));
//		Query<GameModel> q = sessionFactory.getCurrentSession().createQuery(query);
//		List<GameModel> listResult = q.getResultList();
//		GameModel gameModel = listResult.isEmpty() ? null : listResult.get(0);
//		
//		if (gameModel == null) {
//			logger.debug("failed to find game by id: " + gameId);
//		} else {
//			logger.debug("found game by id: " + gameId);
//		}
//		
//		return gameModel == null ? null : new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId(), gameModel.getWinnerId());
//	}
//


//
//	@Override
//	public void forfeitGame(String id, String userId) {
//		logger.debug(String.format("Forfeiting game %s by %s", id, userId));
//		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
//		CriteriaUpdate<GameModel> update = builder.createCriteriaUpdate(GameModel.class);
//		Root root = update.from(GameModel.class);
//        update.set("forfeitId", userId);
//        update.where(builder.equal(root.get("id"), id));
//        int numSessionsDeactivated = sessionFactory.getCurrentSession().createQuery(update).executeUpdate();
//        logger.debug(String.format("Forfeited game %s by %s", id, userId));
//	}
//
//	@Override
//	@Transactional(readOnly = true)
//	public OpenGames getOpenGames() throws Exception {
//		logger.debug("fetching all open games");
//		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
//		CriteriaQuery<ActiveGameModel> query = builder.createQuery(ActiveGameModel.class);
//		Root<ActiveGameModel> root = query.from(ActiveGameModel.class);
//		query.select(root);
//		Query<ActiveGameModel> q = sessionFactory.getCurrentSession().createQuery(query);
//		List<ActiveGameModel> listResult = q.getResultList();
//		List<String> gameIds = listResult.stream()
//						 			 .map(agm -> agm.getGameId())
//						 			 .collect(Collectors.toList());
//		
//		if (CollectionUtils.isEmpty(gameIds)) {
//			return new OpenGames(Collections.emptyList());
//		}
//		
//		CriteriaBuilder gameCriteriaBuilder = sessionFactory.getCriteriaBuilder();
//		CriteriaQuery<GameModel> gameCriteriaQuery = gameCriteriaBuilder.createQuery(GameModel.class);
//		Root<GameModel> gameRoot = gameCriteriaQuery.from(GameModel.class);
//		gameCriteriaQuery.select(gameRoot).where(builder.and(gameRoot.get("id").in(gameIds), gameRoot.get("blackId").isNull()));
//		Query<GameModel> gameQuery = sessionFactory.getCurrentSession().createQuery(gameCriteriaQuery);
//		List<GameModel> gameListResult = gameQuery.getResultList();
//		return new OpenGames(gameListResult.stream()
//							 .map(gameModel -> new Game(gameModel.getId(), gameModel.getState(), getGameStatus(gameModel), gameModel.getRedId(), gameModel.getBlackId(), gameModel.getWinnerId()))
//							 .collect(Collectors.toList()));
//	}
}
