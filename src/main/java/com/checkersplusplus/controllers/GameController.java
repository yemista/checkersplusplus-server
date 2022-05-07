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

import com.checkersplusplus.controllers.inputs.GameMoveInput;
import com.checkersplusplus.controllers.inputs.JoinGameInput;
import com.checkersplusplus.controllers.inputs.SecurityInput;
import com.checkersplusplus.exceptions.ErrorCodes;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;
import com.checkersplusplus.util.ResponseUtil;

@RestController
@RequestMapping("/api/game")
public class GameController {
	
	private static final Logger logger = Logger.getLogger(GameController.class);
	
	@Autowired
	private GameService gameService;
	
	@PostMapping(value = "move", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity movePiece(@RequestBody GameMoveInput payload) {
		try {
			logger.debug(String.format("Attempting to move piece in game %s for sessionId %s", payload.getGameId(), payload.getSessionId()));
			
			if (!gameService.hasActiveGame(payload.getSessionId())) {
				logger.debug(String.format("Cannot move piece for game %s and session %s because user not in active game",
						payload.getGameId(), payload.getSessionId()));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			Game game = gameService.getActiveGame(payload.getSessionId());
			
			if (game == null) {
				logger.error("Verified but failed to get active game for sessionId: " + payload.getSessionId());
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug("Created game for sessionId: " + payload.getSessionId());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unexpectedError(e);
		}
	}
	
	@PostMapping(value = "forfeit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity forfeit(@RequestBody SecurityInput payload) {
		try {
			logger.debug("Attempting to create game for sessionId: " + payload.getToken());
			
			if (!gameService.hasActiveGame(payload.getToken())) {
				logger.debug(String.format("Cannot forfeit game for session %s because there is no active game", payload.getToken()));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			Game game = gameService.getActiveGame(payload.getToken());
			
			if (game == null) {
				logger.debug("Failed to forfeit game for sessionId: " + payload.getToken() + " because game was null");
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug("Created game for sessionId: " + payload.getToken());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unexpectedError(e);
		}
	}
	
	@GetMapping(value = "getOpen", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getOpenGames(@RequestBody SecurityInput payload) {
		try {
			logger.debug("Attempting to get open games for sessionId: " + payload.getToken());
			OpenGames games = gameService.getOpenGames(payload.getToken());
			logger.debug("Got open games for sessionId: " + payload.getToken());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .body(games.convertToJson());
		} catch (Exception e) {
			logger.debug("Exception occurred during get open games: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unexpectedError(e);
		}
	}
	
	@PostMapping(value = "join", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity joinGame(@RequestBody JoinGameInput payload) {
		try {
			logger.debug(String.format("Attempting to join game %s for session %s ", payload.getGameId(), payload.getTokenId()));
			Game game = gameService.joinGame(payload.getTokenId(), payload.getGameId());
			
			if (game == null) {
				logger.debug(String.format("Failed to join game %s for session %s ", payload.getGameId(), payload.getTokenId()));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.CANNOT_JOIN_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug(String.format("Joined game %s for session %s ", payload.getGameId(), payload.getTokenId()));
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during join game: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unexpectedError(e);
		}
	}
	
	@GetMapping(value = "getActive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getActiveGame(@RequestBody SecurityInput payload) {
		try {
			logger.debug("Attempting to get active game for sessionId: " + payload.getToken());
			
			if (!gameService.hasActiveGame(payload.getToken())) {
				logger.debug("Failed to find active game for session " + payload.getToken());
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			Game game = gameService.getActiveGame(payload.getToken());
			logger.debug("Fetched active game: " + game.getId() + " for sessionId: " + payload.getToken());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .body(game.convertToJson());
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unexpectedError(e);
		}
	}

	@PostMapping(value = "create", consumes = MediaType.APPLICATION_JSON_VALUE) 
	public ResponseEntity createGame(@RequestBody SecurityInput payload) {
		try {
			logger.debug("Attempting to create game for sessionId: " + payload.getToken());
			
			if (gameService.hasActiveGame(payload.getToken())) {
				logger.debug(String.format("Session %s already has an active game", payload.getToken()));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.ACTIVE_GAME_EXISTS);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			Game game = gameService.createGame(payload.getToken());
			
			if (game == null) {
				logger.debug("Failed to create game for sessionId: " + payload.getToken());
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.CANNOT_CREATE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug("Created game for sessionId: " + payload.getToken());
			return ResponseEntity
	    			.status(HttpStatus.OK)
	                .build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseUtil.unexpectedError(e);
		}
	}
}
