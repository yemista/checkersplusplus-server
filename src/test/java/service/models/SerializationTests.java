package service.models;

import java.util.UUID;

import org.junit.Test;

import com.checkersplusplus.service.enums.GameStatus;
import com.checkersplusplus.service.models.Game;

public class SerializationTests {
	
	@Test
	public void serializeGameTest() {
		String id = UUID.randomUUID().toString();
		com.checkersplusplus.engine.Game gameEngine = new com.checkersplusplus.engine.Game();
		String state = gameEngine.getGameState();
		String redId = UUID.randomUUID().toString();
		Game game = new Game(id, state, GameStatus.PENDING, redId, null);
		String json = game.convertToJson();
		System.out.println(json);
	}
}
