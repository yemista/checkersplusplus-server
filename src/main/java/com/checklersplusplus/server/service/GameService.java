package com.checklersplusplus.server.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.entities.Game;

@Service
@Transactional
public class GameService {

	@Autowired
	public GameRepository gameRepository;
	
	@Autowired
	public AccountRepository accountRepository;
	

	
	public void forefeitGame(UUID gameId, UUID sessionId) {
		
	}



	public Optional<Game> findByGameId(UUID gameId) {
		// TODO Auto-generated method stub
		return null;
	}
}
