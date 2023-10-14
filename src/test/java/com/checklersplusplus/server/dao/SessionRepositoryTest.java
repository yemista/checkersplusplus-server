package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

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
}
