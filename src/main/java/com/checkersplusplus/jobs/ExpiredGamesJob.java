package com.checkersplusplus.jobs;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.SessionService;
import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.Session;

public class ExpiredGamesJob {

	private static final Logger logger = Logger.getLogger(ExpiredGamesJob.class);
	
	public static final int MINUTES_BETWEEN_JOB_EXECUTION = 3;
	
	@Autowired
	private GameService gameService;
	
	@Autowired
	private SessionService sessionService;
	
	/**
	 * Algorithm:
	 * 	1. Get all active sessions where now - heartbeat > 3 * heartbeat.
	 * 	2. Mark such sessions as inactive
	 * 
	 *  Part 2:
	 *   1. Get all games where now - last_modified > 7 * heartbeat
	 *   2. If both players do not have an active session, mark game as abandoned
	 *      If one player does not have an active session, mark game as forfeit by inactive player
	 */
	public void execute() {
		List<Game> activeGames = gameService.getActiveGames();
		List<String> activeGamesToDelete = new ArrayList<>();
		
		for (Game game : activeGames) {
			if (GameStatus.COMPLETE == game.getStatus()
					|| GameStatus.ABORTED == game.getStatus()
					|| GameStatus.CANCELED == game.getStatus()) {
				activeGamesToDelete.add(game.getId());
			}
			
			if (GameStatus.PENDING == game.getStatus()) {
				Session redSession = sessionService.getLatestActiveSessionByUserId(game.getRedId());
				
				if (redSession.isExpired()) {
					gameService.cancelGame(game.getId());
					activeGamesToDelete.add(game.getId());
				}
			}
			
			if (GameStatus.RUNNING == game.getStatus()) {
				Session redSession = sessionService.getLatestActiveSessionByUserId(game.getRedId());
				Session blackSession = sessionService.getLatestActiveSessionByUserId(game.getBlackId());
				
				if (redSession.isExpired() && blackSession.isExpired()) {
					gameService.cancelGame(game.getId());
				} else if (redSession.isExpired()) {
					gameService.forfeitGame(game.getId(), redSession.getUserId());
				} else if (blackSession.isExpired()) {
					gameService.forfeitGame(game.getId(), blackSession.getUserId());
				} else {
					continue;
				}
				
				activeGamesToDelete.add(game.getId());
			}
		}
	}
}
