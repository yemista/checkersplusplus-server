package com.checkersplusplus.service.models;

import com.checkersplusplus.service.models.serializers.GameSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class Jsonifiable {

	public String convertToJson() {
		GsonBuilder gson = new GsonBuilder();
		gson.registerTypeAdapter(Game.class, new GameSerializer());
		Gson parser = gson.create();
		return parser.toJson(this);
	}
}
