package com.checklersplusplus.server.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "account")
public class AccountModel {

	@Id
	@Column(name = "account_id")
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID accountId;
	
	@Column(name = "email", unique = true)
	private String email;
	
	@Column(name = "username", unique = true)
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "created")
	private LocalDateTime created;
	
	@Column(name = "verified")
	private LocalDateTime verified;
	
	@Column(name = "banned")
	private boolean banned;
	
	@Column(name = "bot")
	private boolean bot;
	
	@Column(name = "tutorial")
	private boolean tutorial;

	public AccountModel() {
		
	}
	
	public AccountModel(UUID accountId, String email, String username, String password, LocalDateTime created,
			LocalDateTime verified) {
		super();
		this.accountId = accountId;
		this.email = email;
		this.username = username;
		this.password = password;
		this.created = created;
		this.verified = verified;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public LocalDateTime getVerified() {
		return verified;
	}

	public void setVerified(LocalDateTime verified) {
		this.verified = verified;
	}

	public boolean isBanned() {
		return banned;
	}

	public void setBanned(boolean banned) {
		this.banned = banned;
	}

	public boolean isBot() {
		return bot;
	}

	public void setBot(boolean bot) {
		this.bot = bot;
	}

	public boolean isTutorial() {
		return tutorial;
	}

	public void setTutorial(boolean tutorial) {
		this.tutorial = tutorial;
	}
}
