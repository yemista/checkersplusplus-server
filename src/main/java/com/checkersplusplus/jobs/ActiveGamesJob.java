package com.checkersplusplus.jobs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.checkersplusplus.dao.GameDao;
import com.checkersplusplus.dao.SessionDao;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Session;

public class ActiveGamesJob implements Job {

	private static final Logger logger = Logger.getLogger(ActiveGamesJob.class);
	
	public static final int MINUTES_BETWEEN_JOB_EXECUTION = 3;
	
	@Autowired
	private GameDao gamesDao;
	
	@Autowired
	private SessionDao sessionDao;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<Game> activeGames = gamesDao.getActiveGames();
		List<String> activeGamesToDelete = new ArrayList<>();
		
		for (Game game : activeGames) {
			if (GameStatus.COMPLETE == game.getStatus()
					|| GameStatus.ABORTED == game.getStatus()
					|| GameStatus.CANCELED == game.getStatus()) {
				activeGamesToDelete.add(game.getId());
			}
			
			if (GameStatus.PENDING == game.getStatus()) {
				Session redSession = sessionDao.getLatestActiveSessionByUserId(game.getRedId());
				
				if (redSession.isExpired()) {
					gamesDao.forfeitGame(game.getId(), redSession.getUserId());
					activeGamesToDelete.add(game.getId());
				}
			}
			
			if (GameStatus.RUNNING == game.getStatus()) {
				Session redSession = sessionDao.getLatestActiveSessionByUserId(game.getRedId());
				Session blackSession = sessionDao.getLatestActiveSessionByUserId(game.getBlackId());
				
				if (redSession.isExpired() && blackSession.isExpired()) {
					if (redSession.isOlder(blackSession)) {
						gamesDao.forfeitGame(game.getId(), redSession.getUserId());
					} else {
						gamesDao.forfeitGame(game.getId(), blackSession.getUserId());
					}
				} else if (redSession.isExpired()) {
					gamesDao.forfeitGame(game.getId(), redSession.getUserId());
				} else if (blackSession.isExpired()) {
					gamesDao.forfeitGame(game.getId(), blackSession.getUserId());
				} else {
					continue;
				}
				
				activeGamesToDelete.add(game.getId());
			}
		}
	}
}
