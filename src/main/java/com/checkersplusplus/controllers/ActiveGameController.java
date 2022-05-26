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
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.checkersplusplus.service.models.Game;

@WebServlet(name = "ActiveGameController", urlPatterns = "/api/game/active")
public class ActiveGameController extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(ActiveGameController.class);
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	// Create a game
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
			
			if (gameService.hasActiveGame(token)) {
				logger.debug(String.format("Session %s already has an active game", token));
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.ACTIVE_GAME_EXISTS);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			Game game = gameService.createGame(token);
			
			if (game == null) {
				logger.debug("Failed to create game for sessionId: " + token);
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.CANNOT_CREATE_GAME);
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
	
	// Get active game for user, if it exists
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
			logger.debug("Attempting to get active game for sessionId: " + token);
			
			if (!gameService.hasActiveGame(token)) {
				logger.debug("Failed to find active game for session " + token);
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.NO_ACTIVE_GAME);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			Game game = gameService.getActiveGame(token);
			logger.debug("Fetched active game: " + game.getId() + " for sessionId: " + token);
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
}
