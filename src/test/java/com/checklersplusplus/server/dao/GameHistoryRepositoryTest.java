package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.checkersplusplus.engine.enums.Color;
import com.checklersplusplus.server.model.GameModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class GameHistoryRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private GameHistoryRepository gameHistoryRepository;
	
	@Test
	public void canGetGamesByBlackId() {
		UUID accountId = UUID.randomUUID();
		createHistoricalGameForAccountId(accountId, Color.BLACK);
		createHistoricalGameForAccountId(accountId, Color.BLACK);
		createHistoricalGameForAccountId(accountId, Color.BLACK);
		Page<GameModel> games = gameHistoryRepository.findByRedIdOrBlackId(accountId, PageRequest.of(0, 2));
		assertThat(games.getContent().size()).isEqualTo(2);
	}
	
	@Test
	public void canGetGamesByRedId() {
		UUID accountId = UUID.randomUUID();
		createHistoricalGameForAccountId(accountId, Color.RED);
		createHistoricalGameForAccountId(accountId, Color.RED);
		createHistoricalGameForAccountId(accountId, Color.RED);
		Page<GameModel> games = gameHistoryRepository.findByRedIdOrBlackId(accountId, PageRequest.of(0, 2));
		assertThat(games.getContent().size()).isEqualTo(2);
	}
	
	@Test
	public void canGetGamesByMixedId() {
		UUID accountId = UUID.randomUUID();
		createHistoricalGameForAccountId(accountId, Color.RED);
		createHistoricalGameForAccountId(accountId, Color.BLACK);
		createHistoricalGameForAccountId(UUID.randomUUID(), Color.RED);
		Page<GameModel> games = gameHistoryRepository.findByRedIdOrBlackId(accountId, PageRequest.of(0, 5));
		assertThat(games.getContent().size()).isEqualTo(2);
	}
	
	private void createHistoricalGameForAccountId(UUID accountId, Color color) {
		GameModel game = new GameModel();
		
		if (color == color.RED) {
			game.setRedId(accountId);
		}
		
		if (color == color.BLACK) {
			game.setBlackId(accountId);
		}
		
		game.setActive(false);
		game.setBlackRating(800);
		game.setCreated(LocalDate.now());
		game.setCreatorRating(800);
		game.setCurrentMoveNumber(20);
		game.setGameState("");
		game.setInProgress(false);
		game.setLastModified(LocalDateTime.now());
		game.setRedRating(800);
		game.setWinnerId(accountId);
		gameRepository.save(game);
	}
}
