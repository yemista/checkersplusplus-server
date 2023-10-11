package com.checklersplusplus.server.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

	public void emailVerificationCode(UUID accountId, String verificationCode) {
		
	}
}
