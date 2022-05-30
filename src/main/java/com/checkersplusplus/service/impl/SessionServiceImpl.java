package com.checkersplusplus.service.impl;

import java.util.Date;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.checkersplusplus.dao.SessionRepository;
import com.checkersplusplus.dao.models.SessionModel;
import com.checkersplusplus.service.SessionService;
import com.checkersplusplus.service.models.Session;

@Component
public class SessionServiceImpl implements SessionService {

	private static final Logger logger = Logger.getLogger(SessionServiceImpl.class);
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Override
	public String createUserSession(String userId) {
		logger.debug(String.format("Creating session for user %s", userId));
		sessionRepository.invalidateExistingSessions(userId);
		SessionModel session = new SessionModel();
		session.setActive(Boolean.TRUE);
		session.setCreateDate(new Date());
		session.setToken(UUID.randomUUID().toString());
		session.setUserId(userId);
		session.setHeartbeat(new Date());
		sessionRepository.save(session);
		logger.debug(String.format("Created session %s for user %s", session.getToken(), userId));
		return session.getToken();
	}

	@Override
	public Session getSessionByTokenId(String tokenId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Session getLatestActiveSessionByUserId(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

}
