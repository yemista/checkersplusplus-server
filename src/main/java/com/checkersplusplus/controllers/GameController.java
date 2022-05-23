package com.checkersplusplus.controllers;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.checkersplusplus.exceptions.ErrorCodes;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.checkersplusplus.service.models.Game;
import com.checkersplusplus.service.models.OpenGames;

@WebServlet(name = "GameController", urlPatterns = "/api/game/action")
public class GameController extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(GameController.class);
	
	@Autowired
	private GameService gameService;
	
	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String token = request.getParameter("token");
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(error.convertToJson());
			return;
		}
		
		String gameId = request.getParameter("game");
		
		if (StringUtils.isBlank(gameId)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_GAME);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(error.convertToJson());
			return;
		}
		
		try {
			logger.debug(String.format("Attempting to move piece in game %s for sessionId %s", gameId, token));
			
			if (!gameService.hasActiveGame(token)) {
				logger.debug(String.format("Cannot move piece for game %s and session %s because user not in active game",
						gameId, token));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			Game game = gameService.getActiveGame(token);
			
			if (game == null) {
				logger.error("Verified but failed to get active game for sessionId: " + token);
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			logger.debug("Created game for sessionId: " + token);
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			out.print("An unknown error has occurred");
			return;
		}
	}
	
	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String token = request.getParameter("token");
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(error.convertToJson());
			return;
		}
		
		try {
			logger.debug("Attempting to create game for sessionId: " + token);
			
			if (!gameService.hasActiveGame(token)) {
				logger.debug(String.format("Cannot forfeit game for session %s because there is no active game", token));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			Game game = gameService.getActiveGame(token);
			
			if (game == null) {
				logger.debug("Failed to forfeit game for sessionId: " + token + " because game was null");
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			gameService.forfeit(game, token);
			logger.debug("Forfeited game for game " + game.getId() + " and sessionId " + token);
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} catch (Exception e) {
			logger.debug("Exception occurred during create: " + e.getMessage());
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			out.print("An unknown error has occurred");
			return;
		}
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String token = request.getParameter("token");
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(error.convertToJson());
			return;
		}
		
		try {
			logger.debug("Attempting to get open games for sessionId: " + token);
			OpenGames games = gameService.getOpenGames(token);
			logger.debug("Got open games for sessionId: " + token);
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} catch (Exception e) {
			logger.debug("Exception occurred during get open games: " + e.getMessage());
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			out.print("An unknown error has occurred");
			return;
		}
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		String token = request.getParameter("token");
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_TOKEN);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(error.convertToJson());
			return;
		}
		
		String gameId = request.getParameter("game");
		
		if (StringUtils.isBlank(token)) {
			CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_GAME);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			out.print(error.convertToJson());
			return;
		}
		
		try {
			logger.debug(String.format("Attempting to join game %s for session %s ", gameId, token));
			Game game = gameService.joinGame(token, gameId);
			
			if (game == null) {
				logger.debug(String.format("Failed to join game %s for session %s ", gameId, token));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.CANNOT_JOIN_GAME);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			logger.debug(String.format("Joined game %s for session %s ", gameId, token));
			response.setStatus(HttpServletResponse.SC_OK);
			return;
		} catch (Exception e) {
			logger.debug("Exception occurred during join game: " + e.getMessage());
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			out.print("An unknown error has occurred");
			return;
		}
	}

	
}
