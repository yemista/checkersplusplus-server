package com.checklersplusplus.server.util;

import java.util.List;

import com.checkersplusplus.engine.CoordinatePair;

public class MoveUtil {
	
	public static String convertCoordinatePairsToString(List<CoordinatePair> pairs) {
		StringBuilder sb = new StringBuilder();
		
		for (CoordinatePair move : pairs) {
			sb.append(String.format("c:%d,r:%d-c:%d,r:%d+", move.getStart().getCol(), move.getStart().getRow(), move.getEnd().getCol(), move.getEnd().getRow()));
		}
		
		return sb.toString();
	}
}
