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
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.entities.Game;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.net.Move;

@RestController
@RequestMapping("/checkersplusplus/api/game")
public class GameController {

	@Autowired
	private GameRepository gameRepository;
	
	@GetMapping("/{gameId}")
	public ResponseEntity<Game> getGameById(@PathVariable("gameId") UUID gameId) {
	    Optional<GameModel> gameData = gameRepository.findById(gameId);

	    if (gameData.isPresent()) {
	      return new ResponseEntity<>(Game.fromModel(gameData.get()), HttpStatus.OK);
	    } else {
	      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	    }
	}
	
	@PostMapping("/{gameId}/move")
	public ResponseEntity<Game> move(@PathVariable("gameId") UUID gameId, @RequestBody List<Move> moves) {
		Optional<GameModel> gameData = gameRepository.findById(gameId);

	    if (gameData.isPresent()) {
	    	List<CoordinatePair> coordinates = moves.stream()
	    			.map(move -> new CoordinatePair(new Coordinate(move.getStartCol(), move.getStartRow()), new Coordinate(move.getEndCol(), move.getEndRow())))
	    			.collect(Collectors.toList());
	    	Board board = new Board(gameData.get().getGameState());
	    	
	    	if (Board.isMoveLegal(board, coordinates)) {
	    		//board.commitMoves(moves);
	    		gameData.get().setGameState(board.getBoardState());
	    		
	    	} else {
	    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	    	}
	    } 
	    
	    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
}
