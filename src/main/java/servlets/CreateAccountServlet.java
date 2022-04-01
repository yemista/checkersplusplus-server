package servlets;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import logging.Logger;
import logic.CreateAccountLogic;
import responses.ErrorResponse;
import responses.SuccessResponse;

public class CreateAccountServlet extends HttpServlet {
	
	private static final String EMAIL_PARAM_NAME = "email";
	private static final String PASSWORD_PARAM_NAME = "password";
	private static final String ALIAS_PARAM_NAME = "alias";
	
	private Logger logger = new Logger();
	
	@Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			response.setContentType("application/json;charset=UTF-8");
			JsonObject data = new Gson().fromJson(request.getReader(), JsonObject.class);
			String email = data.get(EMAIL_PARAM_NAME).getAsString();
			String password = data.get(PASSWORD_PARAM_NAME).getAsString();
			String alias = data.get(ALIAS_PARAM_NAME).getAsString();
			
			if (StringUtils.isBlank(email) 
					|| StringUtils.isBlank(password)
					|| StringUtils.isBlank(alias)) {
				sendErrorResponse(response, "Please fill out all fields");
				return;
			}
			
			CreateAccountLogic logic = new CreateAccountLogic(email, password, alias);
			
			if (!logic.isEmailUnique()) {
				sendErrorResponse(response, "Provided email address is already in use");
				return;
			}
			
			if (!logic.isAliasUnique()) {
				sendErrorResponse(response, "Provided alias is already in use");
				return;
			}
			
			if (!logic.isPasswordSafe()) {
				sendErrorResponse(response, "Password must be at least 8 characters long and contain only letters and numbers");
				return;
			}

			if (logic.createAccount()) {
				sendSuccessResponse(response);
			} else {
				sendErrorResponse(response, "Unable to create account. Please try again later");
			}
		} catch (Exception e) {
			logger.log(e, "Unexpected error occured");
			sendErrorResponse(response, "Unexpected error occured");
		}
    }
	
	private void sendSuccessResponse(HttpServletResponse response) throws IOException {
		ServletOutputStream out = response.getOutputStream();
		SuccessResponse success = new SuccessResponse();
        out.print(success.toString());
	}

	private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
		ServletOutputStream out = response.getOutputStream();
		ErrorResponse errorResponse = new ErrorResponse(message);
        out.print(errorResponse.toString());
	}
}
