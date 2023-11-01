package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.model.SessionModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class SessionRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Test
	public void canInactiveExistingSessions() {
		UUID accountId = UUID.randomUUID();
		SessionModel session1 = new SessionModel();
		session1.setAccountId(accountId);
		session1.setActive(true);
		sessionRepository.save(session1);
		assertThat(session1.getSessionId()).isNotNull();
		
		Optional<SessionModel> session = sessionRepository.getActiveByAccountId(accountId);
		assertThat(session.isPresent()).isTrue();
		
		sessionRepository.inactiveExistingSessions(accountId);
		
		Optional<SessionModel> activeSession = sessionRepository.getActiveByAccountId(accountId);
		assertThat(activeSession.isPresent()).isFalse();
	}
	
	@Test
	public void canGetBySessionId() {
		UUID accountId = UUID.randomUUID();
		SessionModel session = new SessionModel();
		session.setAccountId(accountId);
		session.setActive(true);
		sessionRepository.save(session);
		assertThat(session.getSessionId()).isNotNull();
		
		Optional<SessionModel> fetchedSession = sessionRepository.getActiveBySessionId(session.getSessionId());
		assertThat(fetchedSession).isPresent();
		assertThat(fetchedSession.get().getAccountId()).isEqualTo(accountId);
	}
	
	@Test
	public void canInvalidateSessionsBySessionIds() {
		UUID accountId = UUID.randomUUID();
		SessionModel session = new SessionModel();
		session.setAccountId(accountId);
		session.setActive(true);
		session.setLastModified(LocalDateTime.now());
		sessionRepository.save(session);
		assertThat(session.getSessionId()).isNotNull();
		
		UUID accountId2 = UUID.randomUUID();
		SessionModel session2 = new SessionModel();
		session2.setAccountId(accountId2);
		session2.setLastModified(LocalDateTime.now());
		session2.setActive(true);
		sessionRepository.save(session2);
		assertThat(session2.getSessionId()).isNotNull();
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		List<SessionModel> oldSessions = sessionRepository.getActiveSessionsOlderThan(LocalDateTime.now());
		assertThat(oldSessions.size()).isEqualTo(2);
		
		sessionRepository.invalidateSessionsBySessionIds(Arrays.asList(session.getSessionId(), session2.getSessionId()));
		
		List<SessionModel> activeSessions = sessionRepository.getActiveSessionsOlderThan(LocalDateTime.now());
		assertThat(activeSessions.size()).isEqualTo(0);
	}
}
