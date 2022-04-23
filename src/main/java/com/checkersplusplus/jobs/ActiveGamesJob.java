package com.checkersplusplus.jobs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.checkersplusplus.config.AppInitializer;
import com.checkersplusplus.dao.GameDao;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.Game;

public class ActiveGamesJob implements Job {

	private static final Logger logger = Logger.getLogger(AppInitializer.class);
	
	public static final int MINUTES_BETWEEN_JOB_EXECUTION = 3;
	
	@Autowired
	private GameDao gamesDao;
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		List<Game> activeGames = gamesDao.getActiveGames();
		List<String> activeGamesToDelete = new ArrayList<>();
		
		for (Game game : activeGames) {
			if (GameStatus.COMPLETE.toString().equals(game.getStatus())) {
				activeGamesToDelete.add(game.getId());
			}
			
			if (GameStatus.PENDING.toString().equals(game.getStatus())) {
				
			}
		}
	}

}
