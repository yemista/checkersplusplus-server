package com.checklersplusplus.server.service.mail;

import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.model.AccountModel;

@Service
public class EmailService {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
	
	private static final String ADMIN_EMAIL = "admin@checkersplusplus.com";
	
	private static final String VERIFICATION_SUBJECT = "Checkers++ account verification code";
	private static final String VERIFICATION_TEXT = "Here is your Checkers++ verification code: %s";
	
	@Autowired
    private JavaMailSender emailSender;
	
	@Autowired
	private AccountRepository accountRepository;

	public void emailVerificationCode(UUID accountId, String verificationCode) {
		Optional<AccountModel> account = accountRepository.findById(accountId);
		
		if (account.isPresent()) {
			try {
				sendSimpleMessage(account.get().getEmail(), VERIFICATION_SUBJECT, String.format(VERIFICATION_TEXT, verificationCode));
			} catch (Exception e) {
				logger.error(String.format("Failed to send verification code to %s", account.get().getEmail()), e);
			}
		} else {
			logger.error(String.format("Account not found. Could not send verification code to %s", account.get().getEmail()));
		}
	}
	
	public void sendSimpleMessage(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(ADMIN_EMAIL);
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		emailSender.send(message);
	}
}
