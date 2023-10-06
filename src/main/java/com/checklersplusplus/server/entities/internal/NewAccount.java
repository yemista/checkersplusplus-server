package com.checklersplusplus.server.entities.internal;

import java.util.UUID;

public class NewAccount {
	private UUID accountId;
	private String verificationCode;

	public NewAccount(UUID accountId, String verificationCode) {
		this.accountId = accountId;
		this.verificationCode = verificationCode;
	}
	
	public NewAccount() {
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
}
