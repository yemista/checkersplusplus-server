package com.checklersplusplus.server.service.job;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.checklersplusplus.server.service.mail.EmailService;

@Profile("server")
@Service
public class ScheduledEmailService {

	private static final Logger logger = LoggerFactory.getLogger(ScheduledEmailService.class);

	private static final long FIVE_MINUTE_MILLIS = 1000 * 60 * 1;

	private static final String DESTINATION = "admin@checkersplusplus.com";
	
	@Autowired
	private EmailService emailService;
	
	@Scheduled(fixedDelay = FIVE_MINUTE_MILLIS)
	public void sendEmails() {
		Vector<String> emails = ScheduledEmailQueue.getInstance().getEmailsToSend();
		
		while (!emails.isEmpty()) {
			emailService.sendSimpleMessage(DESTINATION, "ALERT from CheckersPlusPlus", emails.remove(0));
		}
	}
}
