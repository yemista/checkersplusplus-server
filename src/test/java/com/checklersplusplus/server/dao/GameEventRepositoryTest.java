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

import com.checklersplusplus.server.model.GameEventModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class GameEventRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private GameEventRepository gameEventRepository;
	
	@Test
	public void canFindActiveEventForAccountIdAndGameId() {
		UUID accountId = UUID.randomUUID();
		UUID gameId = UUID.randomUUID();
		GameEventModel gameEvent = new GameEventModel();
		gameEvent.setActive(true);
		gameEvent.setEvent("event");
		gameEvent.setEventRecipientAccountId(accountId);
		gameEvent.setGameId(gameId);
		gameEventRepository.save(gameEvent);
		assertThat(gameEvent.getGameEventId()).isNotNull();
		Optional<GameEventModel> fetchedRecord = gameEventRepository.findActiveEventForAccountIdAndGameId(accountId, gameId);
		assertThat(fetchedRecord.isPresent()).isTrue();
		assertThat(fetchedRecord.get().getGameEventId()).isEqualTo(gameEvent.getGameEventId());
	}
	
	@Test
	public void canInactivateEventsForRecipient() {
		UUID accountId = UUID.randomUUID();
		UUID gameId = UUID.randomUUID();
		GameEventModel gameEvent = new GameEventModel();
		gameEvent.setActive(true);
		gameEvent.setEvent("event");
		gameEvent.setEventRecipientAccountId(accountId);
		gameEvent.setGameId(gameId);
		gameEventRepository.save(gameEvent);
		gameEventRepository.inactivateEventsForRecipient(accountId);
		Optional<GameEventModel> fetchedRecord = gameEventRepository.findActiveEventForAccountIdAndGameId(accountId, gameId);
		assertThat(fetchedRecord.isPresent()).isFalse();
	}
}
