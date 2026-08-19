package com.checklersplusplus.server.enums;

/**
 * Lifecycle of a game, derived from the active / inProgress / winnerId columns so that
 * clients do not have to reimplement that logic.
 */
public enum GameStatus {

	/** Created and listed, but nobody has joined yet. active = true, inProgress = false. */
	WAITING_FOR_OPPONENT,

	/** Both seats filled and moves are being made. active = true, inProgress = true. */
	IN_PROGRESS,

	/** Finished with a result. active = false and a winner (or the draw marker) is set. */
	COMPLETE,

	/** Withdrawn before anyone joined. active = false with no winner recorded. */
	CANCELED
}
