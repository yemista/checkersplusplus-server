package com.checklersplusplus.server.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rating")
public class RatingModel {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID ratingId;
	
	@Column(name = "account_id")
	private UUID accountId;
	
	@Column(name = "rating")
	private int rating;
	
	public RatingModel() {
		
	}

	public RatingModel(UUID ratingId, UUID accountId, int rating) {
		this.ratingId = ratingId;
		this.accountId = accountId;
		this.rating = rating;
	}

	public UUID getRatingId() {
		return ratingId;
	}

	public void setRatingId(UUID ratingId) {
		this.ratingId = ratingId;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
	
	
}
