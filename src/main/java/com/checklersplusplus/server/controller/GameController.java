package com.checklersplusplus.server.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.checkersplusplus.engine.Board;
import com.checkersplusplus.engine.Coordinate;
import com.checkersplusplus.engine.CoordinatePair;
import com.checklersplusplus.server.entities.Game;
import com.checklersplusplus.server.entities.Move;
import com.checklersplusplus.server.service.GameService;

@RestController
@RequestMapping("/checkersplusplus/api/game")
public class GameController {

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
	
	@PostMapping("/{gameId}/move")
	public ResponseEntity<Game> move(@PathVariable("gameId") UUID gameId, @RequestBody List<Move> moves) {
		Optional<Game> gameData = gameService.findByGameId(gameId);

	    if (gameData.isPresent()) {
	    	List<CoordinatePair> coordinates = moves.stream()
	    			.map(move -> new CoordinatePair(new Coordinate(move.getStartCol(), move.getStartRow()), new Coordinate(move.getEndCol(), move.getEndRow())))
	    			.collect(Collectors.toList());
	    	com.checkersplusplus.engine.Game logicalGame = new com.checkersplusplus.engine.Game(gameData.get().getGameState());
	    	
	    	if (Board.isMoveLegal(logicalGame.getBoard(), coordinates)) {
	    		logicalGame.doMove(coordinates);
	    		gameData.get().setGameState(logicalGame.getGameState());
	    		
	    	} else {
	    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	    	}
	    } 
	    
	    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
