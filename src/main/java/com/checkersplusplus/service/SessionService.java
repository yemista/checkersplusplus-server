package com.checkersplusplus.service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.SessionRepository;
import com.checkersplusplus.dao.models.SessionModel;
import com.checkersplusplus.exceptions.CheckersPlusPlusException;
import com.checkersplusplus.exceptions.ErrorCodes;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.checkersplusplus.service.models.Session;

@Service
@Transactional
public class SessionService {

	private static final Logger logger = Logger.getLogger(SessionService.class);
	
	@Autowired
	private SessionRepository sessionRepository;
	
	public void invalidateSession(String sessionId) {
		Session session = getSession(sessionId);
		
		if (session == null) {
			return;
		}
		
		sessionRepository.invalidateSession(session.getUserId());
	}
	
	public String createUserSession(String userId) {
		logger.debug(String.format("Creating session for user %s", userId));
		int numSessions = sessionRepository.invalidateExistingSessionsByUserId(userId);
		logger.debug(String.format("Invalidated %d sessions for user %s", numSessions, userId));
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
	
	public Session getSession(String token) {
		List<SessionModel> sessionModels = sessionRepository.getSessionByToken(token);
		
		if (CollectionUtils.isEmpty(sessionModels)) {
			return null;
		}
		
		SessionModel sessionModel = sessionModels.get(0);
		return new Session(sessionModel.getUserId(), sessionModel.getToken(), sessionModel.getHeartbeat());
	}

	public Session getLatestActiveSessionByUserId(String userId) {
		List<SessionModel> sessionModels = sessionRepository.getLatestActiveSessionByUserId(userId);
		
		if (CollectionUtils.isEmpty(sessionModels)) {
			return null;
		}
		
		SessionModel sessionModel = sessionModels.get(0);
		return new Session(sessionModel.getUserId(), sessionModel.getToken(), sessionModel.getHeartbeat());
	}

	public Session validateSession(String token) throws Exception {
		if (StringUtils.isBlank(token)) {
			throw new CheckersPlusPlusException(new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN));
		}
		
		Session session = getSession(token);
		
		if (session == null) {
			logger.debug("No active session for session id: " + token);
			throw new CheckersPlusPlusException(new CheckersPlusPlusError(ErrorCodes.SESSION_EXPIRED));
		}
		
		return session;
	}
}
