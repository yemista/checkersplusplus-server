package com.checklersplusplus.server.service;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.service.mail.EmailService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmailServiceTest {
	
	@Autowired
    private EmailService emailService;

	public void emailVerificationCode(UUID accountId, String verificationCode) {
		
	}
	
	private void sendSimpleMessage(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("admin@checkersplusplus.com");
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		emailService.sendSimpleMessage("elias.kopsiaftis@gmail.com", "test", "hello");
	}
	
	@Test
	public void testEmailService() {
		sendSimpleMessage("elias.kopsiaftis@gmail.com", "testing", "here is the test");
	}
	
}
