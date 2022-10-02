package repository;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.checkersplusplus.dao.SessionRepository;
import com.checkersplusplus.dao.TimeRepository;
import com.checkersplusplus.dao.models.SessionModel;
import com.checkersplusplus.service.AccountService;
import com.checkersplusplus.service.models.Login;
import com.checkersplusplus.service.models.User;

import config.TestJpaConfig;
import util.UserNameTestUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestJpaConfig.class }, loader = AnnotationConfigContextLoader.class)
public class SessionRepositoryTests {

	@Autowired
	private AccountService accountService;

	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private TimeRepository timeRepository;

	@Test
	public void assertGetSessionByToken() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		assertNotNull(login);
		List<SessionModel> sessionModel = sessionRepository.getSessionByToken(login.getSessionId());
		assertNotNull(sessionModel);
		assertEquals(sessionModel.size(), 1);
	}

	@Test
	public void assertGetLastActiveSessionByUserId() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		assertNotNull(login);
		List<SessionModel> sessionModel = sessionRepository.getSessionByToken(login.getSessionId());
		assertNotNull(sessionModel);
		assertEquals(sessionModel.size(), 1);
		List<SessionModel> sessionByUserId = sessionRepository.getLatestActiveSessionByUserId(login.getUserId());
		assertNotNull(sessionByUserId);
		assertEquals(sessionByUserId.size(), 1);
		assertEquals(sessionByUserId.get(0).getToken(), sessionModel.get(0).getToken());
	}

	@Test
	public void assertInvalidateExistingSessions() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		createSessionForUser(user.getId());
		createSessionForUser(user.getId());
		createSessionForUser(user.getId());
		List<SessionModel> sessionByUserId = sessionRepository.getLatestActiveSessionByUserId(user.getId());
		assertEquals(sessionByUserId.size(), 3);
		sessionRepository.invalidateExistingSessionsByUserId(user.getId());
		List<SessionModel> sessionByUserIdAfterInvalidation = sessionRepository
				.getLatestActiveSessionByUserId(user.getId());
		assertEquals(sessionByUserIdAfterInvalidation.size(), 0);
	}

	@Test
	public void assertGetAllSessionsWithHeartbeartOlderThan() throws Exception {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		createSessionForUser(user.getId());
		Date currDbTime = timeRepository.getCurrentTimestamp();
		DateTime dateTime1 = new DateTime(currDbTime);
		DateTime dateTimePlusThreeSeconds = dateTime1.plusSeconds(3);
		List<SessionModel> pastSessions = sessionRepository.getAllSessionsWithHeartbeartOlderThan(dateTimePlusThreeSeconds.toDate());
		List<SessionModel> pastSessionsForUser = pastSessions.stream().filter(session -> user.getId().equals(session.getUserId())).collect(Collectors.toList());
		assertEquals(pastSessionsForUser.size(), 1);
		DateTime dateTime2 = new DateTime(currDbTime);
		DateTime dateTimeMinusThreeSeconds = dateTime2.minusSeconds(3);
		List<SessionModel> futureSessions = sessionRepository.getAllSessionsWithHeartbeartOlderThan(dateTimeMinusThreeSeconds.toDate());
		List<SessionModel> futureSessionsForUser = futureSessions.stream().filter(session -> user.getId().equals(session.getUserId())).collect(Collectors.toList());
		assertEquals(futureSessionsForUser.size(), 0);
	}
	
	@Test
	public void assertMarkSessionsInactive() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		List<SessionModel> sessionModel = sessionRepository.getLatestActiveSessionByUserId(login.getUserId());
		assertEquals(sessionModel.size(), 1);
		List<String> sessionIds = new ArrayList<>();
		sessionIds.add(login.getSessionId());
		sessionRepository.markSessionsInactive(sessionIds);
		List<SessionModel> sessionModelAfterUpdate = sessionRepository.getLatestActiveSessionByUserId(login.getUserId());
		assertEquals(sessionModelAfterUpdate.size(), 0);
	}
	
	@Test
	public void assertUpdateHeartbeatForSession() {
		String userName = UserNameTestUtil.getTestUserName();
		String email = String.format("%s@test.com", userName);
		accountService.createAccount(email, "test", userName);
		User user = accountService.getAccount(email);
		Login login = accountService.login(email);
		List<SessionModel> sessionModel = sessionRepository.getSessionByToken(login.getSessionId());
		assertEquals(sessionModel.size(), 1);
		Date originalHeartbeat = sessionModel.get(0).getHeartbeat();
		sessionRepository.updateHeartbeatForSession(login.getSessionId());
		List<SessionModel> sessionModelAfterUpdate = sessionRepository.getSessionByToken(login.getSessionId());
		assertEquals(sessionModelAfterUpdate.size(), 1);
		assertNotEquals(originalHeartbeat, sessionModelAfterUpdate.get(0).getHeartbeat());
	}

	private void createSessionForUser(String userId) {
		SessionModel session = new SessionModel();
		session.setActive(Boolean.TRUE);
		session.setCreateDate(new Date());
		session.setToken(UUID.randomUUID().toString());
		session.setUserId(userId);
		session.setHeartbeat(new Date());
		sessionRepository.save(session);
	}

}
