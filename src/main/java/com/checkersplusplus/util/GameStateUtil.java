package com.checkersplusplus.util;

import org.apache.log4j.Logger;

import com.checkersplusplus.engine.enums.Color;
import com.checkersplusplus.service.models.Game;

public class GameStateUtil {
	
	private static final Logger logger = Logger.getLogger(GameStateUtil.class);
	
	public static String getNextToAct(Game game) {
		String state = game.getState();
		String[] parts = state.split("\\|");
		
		if (parts.length < 2) {
			logger.debug("GameStateUtil.getNextToAct(Game) parsed an invalid game state: " + state);
			return game.getBlackId();
		}
		
		char[] chars = parts[0].toCharArray();
		
		if (chars[1] == Color.BLACK.getSymbol()) {
			return game.getBlackId();
		}
		
		return game.getRedId();
	}
}
