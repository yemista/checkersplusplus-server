package com.checklersplusplus.server.entities.request;

import java.io.Serializable;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateAccount implements Serializable {

	@NotEmpty(message = "Email is required.")
	@Email(message = "The provided email is not a valid email.")
	private String email;
	
	@NotBlank(message = "Password is required.")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", message = "Password must be 8 characters long and contain only uppercase and lowercase letters, and include a number.")  
	private String password;
	
	@NotBlank(message = "Confirmation password is required.")
	private String confirmPassword;
	
	@Size(min = 3, max = 20, message = "Username must be from 3 to 20 characters.")
	@NotBlank(message = "Username is required.")
	private String username;
	
	public CreateAccount(String email, String password, String confirmPassword, String username) {
		super();
		this.email = email;
		this.password = password;
		this.confirmPassword = confirmPassword;
		this.username = username;
	}
	
	public CreateAccount() {
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
