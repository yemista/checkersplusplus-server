package com.checklersplusplus.server.controller;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.checklersplusplus.server.entities.request.CreateGame;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.service.GameService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/checkersplusplus/api/game")
public class GameController {
	
	private static final Logger logger = LoggerFactory.getLogger(GameController.class);
	
	private static final List<String> VALID_SORTS = Arrays.asList("created", "rating");
	private static final List<String> VALID_SORT_DIRECTIONS = Arrays.asList("asc", "desc");
	
	private static final Integer DEFAULT_PAGE_SIZE = 25;
	private static final Integer MAX_PAGE_SIZE = 100;
	private static final String DEFAULT_SORT_DIRECTION = "desc";

	@Autowired
	private GameService gameService;
	
	@GetMapping("/{gameId}")
	public ResponseEntity<Game> getGameById(@PathVariable("gameId") UUID gameId) {
	    Optional<Game> gameData = gameService.findByGameId(gameId);

	    if (gameData.isPresent()) {
	      return new ResponseEntity<>(gameData.get(), HttpStatus.OK);
	    } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }
	}
	
	// TODO test
	@GetMapping("/{sessiondId}/history")
	public ResponseEntity<List<Game>> getGameHistory(@RequestParam String sortDirection, @RequestParam Integer page, @RequestParam Integer pageSize) {
	   if (!VALID_SORT_DIRECTIONS.contains(sortDirection)) {
		   sortDirection = DEFAULT_SORT_DIRECTION;
	   }
	   
	   if (page == null) {
		   page = 0;
	   }
	   
	   if (pageSize == null || pageSize > MAX_PAGE_SIZE) {
		   pageSize = DEFAULT_PAGE_SIZE;
	   }
		   
	   return null;
	}
	
	// TODO test
	@GetMapping("/open")
	public ResponseEntity<List<Game>> getOpenGames(
			@RequestParam Integer ratingLow, 
			@RequestParam Integer ratingHigh,
			@RequestParam String sortBy,
			@RequestParam String sortDirection,
			@RequestParam Integer page,
			@RequestParam Integer pageSize) {
	   try {
		   // TODO test
		   if (ratingLow == null || ratingLow <= 0) {
			   ratingLow = 0;
		   }
		   
		   if (ratingHigh == null || ratingHigh > 10000) {
			   ratingHigh = 10000;
		   }
		   
		   if (!VALID_SORTS.contains(sortBy)) {
			   sortBy = "";
		   }

		   if (!VALID_SORT_DIRECTIONS.contains(sortDirection)) {
			   sortDirection = DEFAULT_SORT_DIRECTION;
		   }
		   
		   if (page == null) {
			   page = 0;
		   }
		   
		   if (pageSize == null || pageSize > MAX_PAGE_SIZE) {
			   pageSize = DEFAULT_PAGE_SIZE;
		   }
		   
		   List<Game> games = gameService.getOpenGames(ratingLow, ratingHigh, sortBy, sortDirection, page, pageSize);
		   return new ResponseEntity<>(games, HttpStatus.OK);
	    } catch(Exception ex) {
	    	logger.error(ex.getMessage());
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/{sessionId}/{gameId}/move")
	public ResponseEntity<Game> move(@PathVariable("sessionId") UUID sessionId, @PathVariable("gameId") UUID gameId, @RequestBody List<Move> moves) {
		try {
			Game updatedGame = gameService.move(sessionId, gameId, moves);
			updatedGame.setMessage("Move successful.");
			return new ResponseEntity<>(updatedGame, HttpStatus.OK);
		} catch(CheckersPlusPlusServerException e) {
			logger.info(e.getMessage());
			Game game = new Game();
			game.setMessage(e.getMessage());
			return new ResponseEntity<>(game, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			Game game = new Game();
			game.setMessage("Server error. Try again soon.");
			return new ResponseEntity<>(game, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/{sessionId}/{gameId}/join")
	public ResponseEntity<Game> join(@PathVariable("sessionId") UUID sessionId, @PathVariable("gameId") UUID gameId) {
		try {
			Game updatedGame = gameService.joinGame(sessionId, gameId);
			updatedGame.setMessage("Game joined.");
			return new ResponseEntity<>(updatedGame, HttpStatus.OK);
		} catch(CheckersPlusPlusServerException e) {
			logger.info(e.getMessage());
			Game game = new Game();
			game.setMessage(e.getMessage());
			return new ResponseEntity<>(game, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			Game game = new Game();
			game.setMessage("Server error. Try again soon.");
			return new ResponseEntity<>(game, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/{sessionId}/create")
	public ResponseEntity<Game> create(@PathVariable("sessionId") UUID sessionId, @Valid @RequestBody CreateGame createGame) {
		try {
			Game createdGame = gameService.createGame(sessionId, createGame.isMoveFirst());
			createdGame.setMessage("Game created.");
			return new ResponseEntity<>(createdGame, HttpStatus.OK);
		} catch(CheckersPlusPlusServerException e) {
			logger.info(e.getMessage());
			Game game = new Game();
			game.setMessage(e.getMessage());
			return new ResponseEntity<>(game, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			Game game = new Game();
			game.setMessage("Server error. Try again soon.");
			return new ResponseEntity<>(game, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/{sessionId}/{gameId}/cancel")
	public ResponseEntity<CheckersPlusPlusResponse> cancel(@PathVariable("sessionId") UUID sessionId, @PathVariable("gameId") UUID gameId) {
		try {
			gameService.cancelGame(sessionId, gameId);
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse("Canceled game.");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch(CheckersPlusPlusServerException e) {
			logger.info(e.getMessage());
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse("Server error. Try again soon.");
			return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/{sessionId}/{gameId}/forfeit")
	public ResponseEntity<CheckersPlusPlusResponse> forfeit(@PathVariable("sessionId") UUID sessionId, @PathVariable("gameId") UUID gameId) {
		try {
			gameService.forfeitGame(sessionId, gameId);
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse("Forfeited game.");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch(CheckersPlusPlusServerException e) {
			logger.info(e.getMessage());
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse(e.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			logger.error(ex.getMessage());
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse("Server error. Try again soon.");
			return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
}
