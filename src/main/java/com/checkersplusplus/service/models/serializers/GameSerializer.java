package com.checkersplusplus.service.models.serializers;

import java.lang.reflect.Type;

import com.checkersplusplus.service.models.Game;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GameSerializer implements JsonSerializer<Game> {

    @Override
    public JsonElement serialize(Game game, Type type, JsonSerializationContext context) {
        JsonObject root = new JsonObject();
        root.addProperty("id", game.getId());
        root.addProperty("state", game.getState());
        root.addProperty("status", game.getStatus().toString());
        root.addProperty("redId", game.getRedId());
        root.addProperty("blackId", game.getBlackId());
        return root;
    }
}
