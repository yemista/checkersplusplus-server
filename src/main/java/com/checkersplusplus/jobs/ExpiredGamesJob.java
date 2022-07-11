package com.checkersplusplus.jobs;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.HeartbeatService;
import com.checkersplusplus.service.SessionService;

public class ExpiredGamesJob {

	private static final Logger logger = Logger.getLogger(ExpiredGamesJob.class);
	
	public static final int MINUTES_BETWEEN_JOB_EXECUTION = 3;
private static final int BATCH_SIZE = 1000;
	
	@Autowired
	private GameService gameService;
	
	@Autowired
	private SessionService sessionService;
	
	@Autowired
	private HeartbeatService heartbeatService;
	
	/**
	 * Algorithm:
	 *   1. Get all games where now - last_modified > 7 * heartbeat
	 *   2. If both players do not have an active session, mark game as abandoned
	 *      If one player does not have an active session, mark game as forfeit by inactive player
	 */
	public void execute() {
		logger.debug("Expired games job running");
//		List<Session> expiredSessions = heartbeatService.getAllExpiredSessions();
//		logger.debug("There are " + expiredSessions.size() + " sessions to expire");
//		List<List<String>> batchesToExpire = new ArrayList<>();
//		int sessionCounter = 0;
//		List<String> sessionsToExpire = new ArrayList<>();
//		
//		for (Session session : expiredSessions) {
//			sessionsToExpire.add(session.getTokenId());
//			sessionCounter++;
//			
//			if (sessionCounter >= BATCH_SIZE) {
//				batchesToExpire.add(sessionsToExpire);
//				sessionCounter = 0;
//				sessionsToExpire = new ArrayList<>();
//			}
//		}
//		
//		logger.debug("There are " + batchesToExpire.size() + " batches to expire");
//		
//		int batchCounter = 1;
//		for (List<String> batchToExpire : batchesToExpire) {
//			heartbeatService.inactivateSessions(batchToExpire);
//			logger.debug("Expired batch " + batchCounter++);
//		}
		
		logger.debug("Expire games job complete");
	}
}
