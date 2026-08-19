package com.checklersplusplus.server.enums;

/**
 * Outcome of a completed game. Null until the game reaches {@link GameStatus#COMPLETE}.
 */
public enum GameResult {

	BLACK_WON,

	RED_WON,

	DRAW
}
