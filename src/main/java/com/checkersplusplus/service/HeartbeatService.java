package com.checkersplusplus.service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.SessionRepository;
import com.checkersplusplus.dao.models.DateItem;
import com.checkersplusplus.dao.models.SessionModel;
import com.checkersplusplus.service.models.Session;

@Service
@Transactional
public class HeartbeatService {
	
	private static final Logger logger = Logger.getLogger(HeartbeatService.class);

	public static final int NUM_HEARTBEATS_FOR_EXPIRATION = 3;
	public static final int SECONDS_PER_HEARTBEAT = 20;

	@Autowired
	private SessionRepository sessionRepository;
	
	public void updateHeartbeat(String sessionId) {
		logger.debug("Updating heartbeat for sessionId: " + sessionId);
		sessionRepository.updateHeartbeatForSession(sessionId);
	}
	
	public List<Session> getAllExpiredSessions() {
		logger.debug("Getting all expired sessions");
		Date expirationDate = calculateExpirationDate();
		List<SessionModel> sessionModels = sessionRepository.getAllSessionsWithHeartbeartOlderThan(expirationDate);
		return sessionModels.stream()
				.map(sessionModel -> new Session(sessionModel.getUserId(), sessionModel.getToken(), sessionModel.getHeartbeat()))
				.collect(Collectors.toList());
	}

	private Date calculateExpirationDate() {
		DateItem now = sessionRepository.getCurrentTimestamp();
		Date nowDate = Date.from(now.getDate());
		DateTime currentDateTime = new DateTime(nowDate);
		currentDateTime.plusSeconds(SECONDS_PER_HEARTBEAT * NUM_HEARTBEATS_FOR_EXPIRATION);
		return currentDateTime.toDate();
	}

	public void inactivateSessions(List<String> sessions) {
		logger.debug("Expiring sessions");
		sessionRepository.markSessionsInactive(sessions);
	}
}
