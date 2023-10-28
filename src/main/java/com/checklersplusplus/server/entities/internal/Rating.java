package com.checklersplusplus.server.entities.internal;

import java.util.UUID;

public class Rating {
	private int rating;
	private UUID accountId;
	
	public Rating(int rating, UUID accountId) {
		this.rating = rating;
		this.accountId = accountId;
	}
	
	public int getRating() {
		return rating;
	}
	
	public void setRating(int rating) {
		this.rating = rating;
	}
	
	public UUID getAccountId() {
		return accountId;
	}
	
	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}
}
