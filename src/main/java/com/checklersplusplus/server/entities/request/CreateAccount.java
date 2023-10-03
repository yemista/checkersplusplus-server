package com.checklersplusplus.server.entities.request;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateAccount implements Serializable {

	@NotEmpty(message = "The email is required.")
	@Email(message = "The email is not a valid email.")
	private String email;
	
	@NotBlank(message = "The password is required.")
	@Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{10,}$", message = "Password must be 10 characters long and combination of uppercase letters, lowercase letters, numbers.")  
	private String password;
	
	@NotBlank(message = "The confirmation password is required.")
	private String confirmPassword;
	
	@Size(min = 3, max = 20, message = "The username must be from 3 to 20 characters.")
	@NotBlank(message = "The username is required.")
	private String username;
	
	public CreateAccount(String email, String password, String confirmPassword, String alias) {
		super();
		this.email = email;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.username = alias;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
