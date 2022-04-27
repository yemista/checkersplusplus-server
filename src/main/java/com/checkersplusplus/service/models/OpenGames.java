package com.checkersplusplus.service.models;

import java.util.List;

import com.google.gson.annotations.Expose;

public class OpenGames extends Jsonifiable {
	
	public OpenGames(List<Game> games) {
		super();
		this.games = games;
	}

	@Expose(serialize = true, deserialize = true)
	public List<Game> games;

	public List<Game> getGames() {
		return games;
	}

	public void setGames(List<Game> games) {
		this.games = games;
	}
}
