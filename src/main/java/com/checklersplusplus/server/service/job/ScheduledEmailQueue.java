package com.checklersplusplus.server.service.job;

import java.util.Vector;

public class ScheduledEmailQueue {

	private static final Vector<String> emailsToSend = new Vector<>();
	private static ScheduledEmailQueue me = new ScheduledEmailQueue();
	
	private ScheduledEmailQueue() {
	}
	
	public static ScheduledEmailQueue getInstance() {
		return me;
	}
	
	public Vector<String> getEmailsToSend() {
		return emailsToSend;
	}
}
