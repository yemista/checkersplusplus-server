package com.checklersplusplus.server.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;

import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.exception.SessionNotFoundException;
import com.checklersplusplus.server.model.SessionModel;

public class GameHistoryService {

	@Autowired
	private SessionRepository sessionRepository;
	
	public List<Game> getGameHistory(UUID sessionId) throws CheckersPlusPlusServerException {
		Optional<SessionModel> sessionModel = sessionRepository.getActiveBySessionId(sessionId);
		
		if (sessionModel.isEmpty()) {
			throw new SessionNotFoundException();
		}
		
		return null;
	}
}
