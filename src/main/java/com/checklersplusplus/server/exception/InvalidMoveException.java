package com.checklersplusplus.server.exception;

import java.util.List;

import com.checkersplusplus.engine.CoordinatePair;

public class InvalidMoveException extends CheckersPlusPlusServerException {
	private List<CoordinatePair> coordinates;
	
	public InvalidMoveException(List<CoordinatePair> coordinates) {
		super("Invalid move.");
		this.coordinates = coordinates;
	}

	public List<CoordinatePair> getCoordinates() {
		return coordinates;
	}

}
