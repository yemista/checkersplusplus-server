package com.checkersplusplus.controllers;

import javax.servlet.http.HttpServlet;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checkersplusplus.controllers.inputs.GameMoveInput;
import com.checkersplusplus.controllers.inputs.SecurityInput;
import com.checkersplusplus.exceptions.ErrorCodes;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.SessionService;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;
import com.checkersplusplus.service.models.Session;

@RestController
@RequestMapping("/api/games")
public class GameController extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(GameController.class);
	
	@Autowired
	private GameService gameService;
	
	@Autowired
	private SessionService sessionService;
	
	@PostMapping(value = "/{gameId}/move", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity move(@PathVariable("gameId") String gameId, @RequestBody GameMoveInput payload) {
		String token = payload.getSessionId();
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error.convertToJson());
		}
		
		try {
			logger.debug(String.format("Attempting to move piece in game %s for sessionId %s", gameId, token));
			
			Game game = gameService.getActiveGame(token);
			
			if (game == null) {
				logger.error("Verified but failed to get active game for sessionId: " + token);
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug("Created game for sessionId: " + token);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("An unknown error has occurred");
		}
	}
	
	@PostMapping(value = "/{gameId}/forfeit", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity forfeit(@PathVariable("gameId") String gameId, @RequestBody SecurityInput payload) {
		String token = payload.getToken();
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error.convertToJson());
		}
		
		try {
			logger.debug("Attempting to forfeit game for sessionId: " + token);
			Game game = gameService.getActiveGame(token);
			
			if (game == null) {
				logger.debug("Failed to forfeit game for sessionId: " + token + " because game was null");
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			Session session = sessionService.getSession(token);
			gameService.forfeitGame(game.getId(), session.getUserId());
			logger.debug("Forfeited game for game " + game.getId() + " and sessionId " + token);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("An unknown error has occurred");
		}
	}
	
	@GetMapping(value = "open", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getOpenGames(@RequestBody SecurityInput payload,
									   @RequestParam(value = "page", required = false) Integer page) {
		String token = payload.getToken();
			
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error.convertToJson());
		}
		
		try {
			logger.debug("Attempting to get open games for sessionId: " + token);
			Session session = sessionService.getSession(token);
			
			if (session == null) {
				logger.debug("Failed to get open games for sessionId: " + token + " because session was null");
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.SESSION_EXPIRED);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			OpenGames games = gameService.getOpenGames(page);
			logger.debug("Got open games for sessionId: " + token);
			return ResponseEntity.status(HttpStatus.OK).body(games.convertToJson());
		} catch (Exception e) {
			logger.debug("Exception occurred during get open games: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("An unknown error has occurred");
		}
	}
	
	@PostMapping(value = "/{gameId}/join", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity join(@PathVariable("gameId") String gameId, @RequestBody SecurityInput payload) {
		String token = payload.getToken();
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error.convertToJson());
		}
		
		try {
			logger.debug(String.format("Attempting to join game %s for session %s ", gameId, token));
			Game game = gameService.joinGame(token, gameId);
			
			if (game == null) {
				logger.debug(String.format("Failed to join game %s for session %s ", gameId, token));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.CANNOT_JOIN_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug(String.format("Joined game %s for session %s ", gameId, token));
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (Exception e) {
			logger.debug("Exception occurred during join game: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("An unknown error has occurred");
		}
	}
	
	@PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity create(@RequestBody SecurityInput payload) {
		String token = payload.getToken();
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error.convertToJson());
		}
		
		try {
			logger.debug("Attempting to create game for sessionId: " + token);
			Game activeGame = gameService.getActiveGame(token);
			
			if (activeGame != null) {
				logger.debug(String.format("Session %s already has an active game", token));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.ACTIVE_GAME_EXISTS);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			Game game = gameService.createGame(token);
			
			if (game == null) {
				logger.debug("Failed to create game for sessionId: " + token);
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.CANNOT_CREATE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug("Created game for sessionId: " + token);
			return ResponseEntity.status(HttpStatus.OK).body(game.convertToJson());
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("An unknown error has occurred");
		}
	}
	
	@GetMapping(value = "active", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity getActiveGame(@RequestBody SecurityInput payload) {
		String token = payload.getToken();
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error.convertToJson());
		}
		
		try {
			logger.debug("Attempting to get active game for sessionId: " + token);
			Game game = gameService.getActiveGame(token);
			
			if (game == null) {
				logger.debug("Failed to find active game for session " + token);
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				return ResponseEntity
	                    .status(HttpStatus.BAD_REQUEST)
	                    .body(error.convertToJson());
			}
			
			logger.debug("Fetched active game: " + game.getId() + " for sessionId: " + token);
			return ResponseEntity.status(HttpStatus.OK).body(game.convertToJson());
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("An unknown error has occurred");
		}
	}
}
