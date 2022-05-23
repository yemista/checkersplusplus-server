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

import com.checkersplusplus.controllers.inputs.CreateUserInput;
import com.checkersplusplus.exceptions.ErrorCodes;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.GameService;
import com.checkersplusplus.service.models.CheckersPlusPlusError;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet(name = "CreateAccountController", urlPatterns = "/api/account/create")
public class CreateAccountController extends HttpServlet {

	private static final Logger logger = Logger.getLogger(CreateAccountController.class);
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GameService gameService;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		JsonObject data = new Gson().fromJson(request.getReader(), JsonObject.class);
		String email = data.get("email").getAsString();
		String alias = data.get("alias").getAsString();
		String password = data.get("password").getAsString();
		CreateUserInput payload = new CreateUserInput(email, alias, password);
		
		try {
			logger.debug("Attempting create user for " + payload.getEmail());
			
			if (createUserInputNotPopulated(payload)) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_CREATE_USER_INPUT);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			if (!accountService.isEmailValid(payload.getEmail())) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_EMAIL);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
			
			if (!accountService.isAliasValid(payload.getAlias())) {
				CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_ALIAS);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
			}
	        
	        if (!accountService.isPasswordSafe(payload.getPassword())) {
	        	CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_PASSWORD);
	        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
	        }
	        
	        if (!accountService.isAliasUnique(payload.getAlias())) {
	        	CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_ALIAS_NON_UNIQUE);
	        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
	        }
	        
	        if (!accountService.isEmailUnique(payload.getEmail())) {
	        	CheckersPlusPlusError error = new CheckersPlusPlusError(ErrorCodes.INVALID_ALIAS_IN_USE);
	        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				out.print(error.convertToJson());
				return;
	        }
	        
	        accountService.createAccount(payload.getEmail(), payload.getPassword(), payload.getAlias());
	        logger.debug("Successfully created account for: " + payload.getEmail());
	        response.setStatus(HttpServletResponse.SC_OK);
		} catch (Exception e) {
			logger.debug("Exception occurred during create account: " + e.getMessage());
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			out.print("An unknown error has occurred");
			return;
		}	
	}
	

	private boolean createUserInputNotPopulated(CreateUserInput payload) {
		return StringUtils.isBlank(payload.getEmail()) 
				|| StringUtils.isBlank(payload.getAlias())
				|| StringUtils.isBlank(payload.getPassword());
	}
}
