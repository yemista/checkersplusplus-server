package com.checklersplusplus.server.entities.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResetPassword {
	
	@NotBlank(message = "Username is required.")
	private String username;
	
	@NotBlank(message = "Password is required.")
	@Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$", message = "Password must be 8 characters long and contain only uppercase and lowercase letters, and include a number.")  
	private String password;
	
	@NotBlank(message = "Confirmation password is required.")
	private String confirmPassword;
	
	@Size(min = 6, max = 6, message = "Invalid verification code.")
	@NotBlank(message = "Verification code is required.")
	private String verificationCode;

	public ResetPassword(String username, String password, String confirmPassword, String verificationCode) {
		this.username = username;
		this.verificationCode = verificationCode;
		this.password = password;
		this.confirmPassword = confirmPassword;
	}

	public ResetPassword() {
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
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
}
