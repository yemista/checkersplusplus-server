package com.checklersplusplus.server.controller.game;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.checkersplusplus.engine.Board;
import com.checklersplusplus.server.controller.GameController;
import com.checklersplusplus.server.entities.request.CreateGame;
import com.checklersplusplus.server.entities.request.Move;
import com.checklersplusplus.server.entities.response.CheckersPlusPlusResponse;
import com.checklersplusplus.server.entities.response.Game;
import com.checklersplusplus.server.entities.response.GameHistory;
import com.checklersplusplus.server.service.GameHistoryService;
import com.checklersplusplus.server.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = GameController.class)
public class GameControllerTest {

	@MockBean
	private GameService gameService;
	
	@MockBean
	private GameHistoryService gameHistoryService;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void canGetOpenGames() throws Exception {
		Game game = new Game();
		List<Game> gameList = new ArrayList<>();
		gameList.add(game);
		Mockito.when(gameService.getOpenGames(any(), any(), any(), any(), any(), any()))
				.thenReturn(gameList);
		ResultActions resultActions = mockMvc.perform(get("/checkersplusplus/api/game/open").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		@SuppressWarnings("unchecked")
		List<Game> response = objectMapper.readValue(contentAsString, List.class);
	}
	
	@Test
	public void canGetGameHistory() throws Exception {
		UUID sessionId = UUID.randomUUID();
		GameHistory gameHistory = new GameHistory();
		List<GameHistory> gameHistoryList = new ArrayList<>();
		gameHistoryList.add(gameHistory);
		Mockito.when(gameHistoryService.getGameHistory(any(), any(), any(), any()))
				.thenReturn(gameHistoryList);
		ResultActions resultActions = mockMvc.perform(get("/checkersplusplus/api/game/history/" + sessionId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		@SuppressWarnings("unchecked")
		List<GameHistory> response = objectMapper.readValue(contentAsString, List.class);
	}
	
	@Test
	public void canGetGameById() throws Exception {
		UUID gameId = UUID.randomUUID();
		UUID blackId = UUID.randomUUID();
		UUID redId = UUID.randomUUID();
		Board board = new Board();
		String gameState = board.getBoardState();
		Mockito.when(gameService.findByGameId(any())).thenReturn(Optional.of(new Game(gameId, gameState, blackId, redId, redId)));
		ResultActions resultActions = mockMvc.perform(get("/checkersplusplus/api/game/" + gameId).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		Game response = objectMapper.readValue(contentAsString, Game.class);
		assertEquals(response.getMessage(), null);
		assertEquals(response.getGameId(), gameId);
		assertEquals(response.getBlackId(), blackId);
		assertEquals(response.getRedId(), redId);
		assertEquals(response.getGameState(), gameState);
	}
	
	@Test
	public void canMove() throws Exception {
		UUID sessionId = UUID.randomUUID();
		UUID gameId = UUID.randomUUID();
		Mockito.when(gameService.move(any(), any(), any())).thenReturn(new Game());
		List<Move> moves = new ArrayList<>();
		moves.add(new Move(0, 0, 0, 0));
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/game/" + sessionId + "/" + gameId + "/move").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(moves)))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		Game response = objectMapper.readValue(contentAsString, Game.class);
		assertEquals(response.getMessage(), "Move successful.");
	}
	
	@Test
	public void canJoinGame() throws Exception {
		UUID sessionId = UUID.randomUUID();
		UUID gameId = UUID.randomUUID();
		Mockito.when(gameService.joinGame(any(), any())).thenReturn(new Game());
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/game/" + sessionId + "/" + gameId + "/join").contentType(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		Game response = objectMapper.readValue(contentAsString, Game.class);
		assertEquals(response.getMessage(), "Game joined.");
	}
	
	@Test
	public void canCreateGame() throws Exception {
		UUID sessionId = UUID.randomUUID();
		Mockito.when(gameService.createGame(any(), Mockito.anyBoolean())).thenReturn(new Game());
		CreateGame createGame = new CreateGame();
		createGame.setMoveFirst(true);
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/game/" + sessionId + "/create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createGame)))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		Game response = objectMapper.readValue(contentAsString, Game.class);
		assertEquals(response.getMessage(), "Game created.");
	}
	
	@Test
	public void canForfeitGame() throws Exception {
		UUID sessionId = UUID.randomUUID();
		UUID gameId = UUID.randomUUID();
		Mockito.doNothing().when(gameService).forfeitGame(any(), any());
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/game/" + sessionId + "/" + gameId + "/forfeit").contentType(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Forfeited game.");
	}
	
	@Test
	public void canCancelGame() throws Exception {
		UUID sessionId = UUID.randomUUID();
		UUID gameId = UUID.randomUUID();
		Mockito.doNothing().when(gameService).cancelGame(any(), any());
		ResultActions resultActions = mockMvc.perform(post("/checkersplusplus/api/game/" + sessionId + "/" + gameId + "/cancel").contentType(MediaType.APPLICATION_JSON))
							.andExpect(status().isOk())
							.andDo(print());
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		CheckersPlusPlusResponse response = objectMapper.readValue(contentAsString, CheckersPlusPlusResponse.class);
		assertEquals(response.getMessage(), "Canceled game.");
	}
}
