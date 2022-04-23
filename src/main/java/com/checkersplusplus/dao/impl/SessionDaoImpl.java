package com.checkersplusplus.dao.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.dao.models.SessionModel;
import com.checkersplusplus.service.models.Session;

@Repository
@Transactional
@Component
public class SessionDaoImpl implements SessionDao {

	private static final Logger logger = Logger.getLogger(SessionDaoImpl.class);
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public String createUserSession(String userId) {
		logger.debug(String.format("Creating session for user %s", userId));
		invalidateExistingSessions(userId);
		SessionModel session = new SessionModel();
		session.setActive(Boolean.TRUE);
		session.setCreateDate(new Date());
		session.setToken(UUID.randomUUID().toString());
		session.setUserId(userId);
		session.setHeartbeat(new Date());
		sessionFactory.getCurrentSession().persist(session);
		logger.debug(String.format("Created session %s for user %s", session.getToken(), userId));
		return session.getToken();
	}

	@Override
	public Session getSessionByTokenId(String tokenId) {
		logger.debug(String.format("Getting session for token %s", tokenId));
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SessionModel> query = builder.createQuery(SessionModel.class);
		Root<SessionModel> root = query.from(SessionModel.class);
		query.select(root).where(builder.equal(root.get("token"), tokenId));
		Query<SessionModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<SessionModel> listResult = q.getResultList();
		SessionModel sessionModel = listResult.isEmpty() ? null : listResult.get(0);
		
		if (sessionModel == null) {
			logger.debug(String.format("Did not find session for token %s", tokenId));
		} else {
			logger.debug(String.format("Found session for token %s", tokenId));
		}
		
		return sessionModel == null ? null : new Session(sessionModel.getUserId(), sessionModel.getToken(), sessionModel.getHeartbeat());
	}
	
	@Override
	public Session getLatestActiveSessionByUserId(String userId) {
		logger.debug(String.format("Getting session for user %s", userId));
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SessionModel> query = builder.createQuery(SessionModel.class);
		Root<SessionModel> root = query.from(SessionModel.class);
		query.select(root)
		     .where(builder.and(builder.equal(root.get("userId"), userId), builder.equal(root.get("active"), true)))
			 .orderBy(Arrays.asList(builder.desc(root.get("createDate"))));
		Query<SessionModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<SessionModel> listResult = q.getResultList();
		SessionModel sessionModel = listResult.isEmpty() ? null : listResult.get(0);

		if (sessionModel == null) {
			logger.debug(String.format("Did not find session for user %s", userId));
		} else {
			logger.debug(String.format("Found session for user %s", userId));
		}
		
		return sessionModel == null ? null : new Session(sessionModel.getUserId(), sessionModel.getToken(), sessionModel.getHeartbeat());
	}

	private void invalidateExistingSessions(String userId) {
		logger.debug(String.format("Invalidating sessions for user %s", userId));
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaUpdate<SessionModel> update = builder.createCriteriaUpdate(SessionModel.class);
		Root root = update.from(SessionModel.class);
        update.set("active", false);
        update.where(builder.equal(root.get("active"), true));
        int numSessionsDeactivated = sessionFactory.getCurrentSession().createQuery(update).executeUpdate();
        logger.debug(String.format("Invalidated %d sessions for user %s", numSessionsDeactivated, userId));
	}

}
