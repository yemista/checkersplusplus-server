package com.checklersplusplus.server.entities.request;

public class CreateGame {
	private boolean moveFirst;

	public CreateGame(boolean moveFirst) {
		this.moveFirst = moveFirst;
	}

	public boolean isMoveFirst() {
		return moveFirst;
	}

	public void setMoveFirst(boolean moveFirst) {
		this.moveFirst = moveFirst;
	}
}
