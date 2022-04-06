package com.checkersplusplus.dao.impl;

import java.util.Date;
import java.util.UUID;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.dao.models.SessionModel;

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
		sessionFactory.getCurrentSession().persist(session);
		return session.getToken();
	}

	private void invalidateExistingSessions(String userId) {
		// TODO Auto-generated method stub
		
	}

}
