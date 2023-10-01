package com.checklersplusplus.server.model;

import java.sql.Timestamp;
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
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID accountId;
	
	@Column(name = "email", unique = true)
	private String email;
	
	@Column(name = "username", unique = true)
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "created")
	private Timestamp created;
	
	@Column(name = "verified")
	private Timestamp verified;

	public AccountModel() {
		
	}
	
	public AccountModel(UUID accountId, String email, String username, String password, Timestamp created,
			Timestamp verified) {
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

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}

	public Timestamp getVerified() {
		return verified;
	}

	public void setVerified(Timestamp verified) {
		this.verified = verified;
	}
}
