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

import com.checkersplusplus.controllers.inputs.LoginInput;
import com.checkersplusplus.exceptions.ErrorCodes;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.checkersplusplus.service.models.Login;

@WebServlet(name = "LoginController", urlPatterns = "/api/account/login")
public class LoginController extends HttpServlet {
	
	private static final Logger logger = Logger.getLogger(LoginController.class);
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		
		try {
			String email = request.getParameter("email");
			String password = request.getParameter("password");
			LoginInput payload = new LoginInput(email, password);
			logger.debug("Attempting login for " + payload.getEmail());
			
			if (loginInputNotPopulated(payload)) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_LOGIN_INPUT);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			if (!accountService.isLoginValid(payload.getEmail(), payload.getPassword())) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_LOGIN);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			Login login = accountService.login(payload.getEmail());			
			logger.debug("Successfully logged in account: " + payload.getEmail());
			logger.debug("Session id is: " + login.getSessionId());
			response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.debug("Exception occurred during login: " + e.getMessage());
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			out.print("An unknown error has occurred");
			return;
		}		
	}
	
	
	private boolean loginInputNotPopulated(LoginInput payload) {
		return StringUtils.isBlank(payload.getEmail())
				|| StringUtils.isBlank(payload.getPassword());
	}

}
