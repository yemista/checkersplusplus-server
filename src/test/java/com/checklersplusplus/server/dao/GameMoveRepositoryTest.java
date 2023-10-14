package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.model.GameMoveModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class GameMoveRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private GameMoveRepository gameMoveRepository;
	
	@Test
	public void canFindFirstByGameIdOrderByMoveNumberDesc() {
		UUID gameId = UUID.randomUUID();
		GameMoveModel gameMove1 = new GameMoveModel();
		gameMove1.setCreated(LocalDateTime.now());
		gameMove1.setAccountId(UUID.randomUUID());
		gameMove1.setGameId(gameId);
		gameMove1.setMoveList("");
		gameMove1.setMoveNumber(1);
		gameMoveRepository.save(gameMove1);
		GameMoveModel gameMove2 = new GameMoveModel();
		gameMove2.setCreated(LocalDateTime.now());
		gameMove2.setAccountId(UUID.randomUUID());
		gameMove2.setGameId(gameId);
		gameMove2.setMoveList("");
		gameMove2.setMoveNumber(2);
		gameMoveRepository.save(gameMove2);
		GameMoveModel gameMove3 = new GameMoveModel();
		gameMove3.setCreated(LocalDateTime.now());
		gameMove3.setAccountId(UUID.randomUUID());
		gameMove3.setGameId(gameId);
		gameMove3.setMoveList("");
		gameMove3.setMoveNumber(3);
		gameMoveRepository.save(gameMove3);
		assertThat(gameMove3.getGameMoveId()).isNotNull();
		Optional<GameMoveModel> fetchedRow = gameMoveRepository.findFirstByGameIdOrderByMoveNumberDesc(gameId);
		assertThat(fetchedRow.isPresent()).isTrue();
		assertThat(fetchedRow.get().getGameMoveId()).isEqualTo(gameMove3.getGameMoveId());
	}

}
