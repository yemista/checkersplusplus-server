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

import com.checklersplusplus.server.model.LastMoveSentModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class LastMoveSentRepositoryTest {
	
	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private LastMoveSentRepository lastMoveSentRepository;
	
	@Test
	public void canFindFirstByAccountIdAndGameIdOrderByLastMoveSentDesc() {
		UUID accountId = UUID.randomUUID();
		UUID gameId = UUID.randomUUID();
		LastMoveSentModel model1 = new LastMoveSentModel();
		model1.setAccountId(accountId);
		model1.setGameId(gameId);
		model1.setLastMoveSent(1);
		lastMoveSentRepository.save(model1);
		LastMoveSentModel model2 = new LastMoveSentModel();
		model2.setAccountId(accountId);
		model2.setGameId(gameId);
		model2.setLastMoveSent(2);
		lastMoveSentRepository.save(model2);
		LastMoveSentModel model3 = new LastMoveSentModel();
		model3.setAccountId(accountId);
		model3.setGameId(gameId);
		model3.setLastMoveSent(3);
		lastMoveSentRepository.save(model3);
		assertThat(model3.getLastMoveSentId()).isNotNull();
		Optional<LastMoveSentModel> fetchedRow = lastMoveSentRepository.findFirstByAccountIdAndGameIdOrderByLastMoveSentDesc(accountId, gameId);
		assertThat(fetchedRow.isPresent()).isTrue();
		assertThat(fetchedRow.get().getLastMoveSentId()).isEqualTo(model3.getLastMoveSentId());
	}
}
