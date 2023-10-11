package com.checklersplusplus.server.controller;

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
	
	// TODO - add filter and sort @RequestParam
	@GetMapping("/open")
	public ResponseEntity<List<Game>> getOpenGames() {
	   try {
		   List<Game> games = gameService.getOpenGames();
		   return new ResponseEntity<>(games, HttpStatus.OK);
	    } catch(Exception ex) {
			return new ResponseEntity<>(Collections.emptyList(), HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
	
	@PostMapping("/{sessionId}/{gameId}/move")
	public ResponseEntity<Game> move(@PathVariable("sessionId") UUID sessionId, @PathVariable("gameId") UUID gameId, @RequestBody List<Move> moves) {
		try {
			Game updatedGame = gameService.move(sessionId, gameId, moves);
			updatedGame.setMessage("Move successful.");
			return new ResponseEntity<>(updatedGame, HttpStatus.OK);
		} catch(CheckersPlusPlusServerException ex) {
			Game game = new Game();
			game.setMessage(ex.getMessage());
			return new ResponseEntity<>(game, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
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
		} catch(CheckersPlusPlusServerException ex) {
			Game game = new Game();
			game.setMessage(ex.getMessage());
			return new ResponseEntity<>(game, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
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
		} catch(CheckersPlusPlusServerException ex) {
			Game game = new Game();
			game.setMessage(ex.getMessage());
			return new ResponseEntity<>(game, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
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
		} catch(CheckersPlusPlusServerException ex) {
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse(ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
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
		} catch(CheckersPlusPlusServerException ex) {
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse(ex.getMessage());
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		} catch(Exception ex) {
			CheckersPlusPlusResponse response = new CheckersPlusPlusResponse("Server error. Try again soon.");
			return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
		}
	}
}
