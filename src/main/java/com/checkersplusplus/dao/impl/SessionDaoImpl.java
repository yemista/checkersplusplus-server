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

import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.dao.models.SessionModel;
import com.checkersplusplus.service.models.Session;

@Repository
@Transactional
@Component
public class SessionDaoImpl implements SessionDao {

	@Autowired
	private SessionFactory sessionFactory;
	
	@Override
	public String createUserSession(String userId) {
		invalidateExistingSessions(userId);
		SessionModel session = new SessionModel();
		session.setActive(Boolean.TRUE);
		session.setCreateDate(new Date());
		session.setToken(UUID.randomUUID().toString());
		session.setUserId(userId);
		session.setHeartbeat(new Date());
		sessionFactory.getCurrentSession().persist(session);
		return session.getToken();
	}

	@Override
	public Session getSessionByTokenId(String tokenId) {
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<SessionModel> query = builder.createQuery(SessionModel.class);
		Root<SessionModel> root = query.from(SessionModel.class);
		query.select(root).where(builder.equal(root.get("token"), tokenId));
		Query<SessionModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<SessionModel> listResult = q.getResultList();
		SessionModel sessionModel = listResult.isEmpty() ? null : listResult.get(0);
		return sessionModel == null ? null : new Session(sessionModel.getUserId(), sessionModel.getToken(), sessionModel.getHeartbeat());
	}

	private void invalidateExistingSessions(String userId) {
		// TODO Auto-generated method stub
		
	}

}
