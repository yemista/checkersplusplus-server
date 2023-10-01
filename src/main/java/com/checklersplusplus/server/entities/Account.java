package com.checklersplusplus.server.entities;

import java.io.Serializable;
import java.util.UUID;

public class Account implements Serializable {
	private UUID accountId;
	private String username;
	
	public Account(UUID accountId, String username) {
		super();
		this.accountId = accountId;
		this.username = username;
	}

	public Account() {
	}

	public UUID getAccountId() {
		return accountId;
	}

	public String getUsername() {
		return username;
	}
}
