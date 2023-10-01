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
@Table(name = "verify_account")
public class VerifyAccountModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID verifyAccountId;
	
	@Column(name = "account_id")
	private UUID accountId;
	
	@Column(name = "verification_code")
	private String verificationCode;
	
	@Column(name = "created")
	private Timestamp created;
	
	@Column(name = "active")
	private boolean active;
	
	public VerifyAccountModel() {
		
	}

	public VerifyAccountModel(UUID verifyAccountId, UUID accountId, String verificationCode, Timestamp created, boolean active) {
		super();
		this.verifyAccountId = verifyAccountId;
		this.accountId = accountId;
		this.verificationCode = verificationCode;
		this.created = created;
		this.active = active;
	}

	public UUID getVerifyAccountId() {
		return verifyAccountId;
	}

	public void setVerifyAccountId(UUID verifyAccountId) {
		this.verifyAccountId = verifyAccountId;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public String getVerificationCode() {
		return verificationCode;
	}

	public void setVerificationCode(String verificationCode) {
		this.verificationCode = verificationCode;
	}

	public Timestamp getCreated() {
		return created;
	}

	public void setCreated(Timestamp created) {
		this.created = created;
	}
	
	public boolean getActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
}
