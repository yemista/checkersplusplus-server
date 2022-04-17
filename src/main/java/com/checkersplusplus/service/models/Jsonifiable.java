package com.checkersplusplus.service.models;

import com.google.gson.Gson;

public abstract class Jsonifiable {

	public String convertToJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
}
