package com.checkersplusplus.controllers;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkersplusplus.controllers.inputs.SecurityInput;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.util.ResponseUtil;

@RestController
@RequestMapping("/api/game")
public class GameController {
	
	private static final Logger logger = Logger.getLogger(GameController.class);
	
	@Autowired
	private GameService gameService;
	
	@GetMapping(value = "getActive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getActiveGame(@RequestBody SecurityInput payload) {
		try {
			logger.debug("Attempting to get active game for sessionId: " + payload.getToken());
			
			if (gameService.hasActiveGame(payload.getToken())) {
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Logged in user already has an active game");
			}
			
			Game game = gameService.getActiveGame(payload.getToken());
			logger.debug("Fetched active game: " + game.getId() + " for sessionId: " + payload.getToken());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .body(game.convertToJson());
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unknownError();
		}
	}

	@PostMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity createGame(@RequestBody SecurityInput payload) {
		try {
			logger.debug("Attempting to create game for sessionId: " + payload.getToken());
			
			if (gameService.hasActiveGame(payload.getToken())) {
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body("Logged in user already has an active game");
			}
			
			Game game = gameService.createGame(payload.getToken());
			
			if (game == null) {
				logger.debug("Failed to create game for sessionId: " + payload.getToken());
				return ResponseEntity
		    			.status(HttpStatus.BAD_REQUEST)
		                .body("Unable to create game");
			}
			
			logger.debug("Created game for sessionId: " + payload.getToken());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unknownError();
		}
	}
}
