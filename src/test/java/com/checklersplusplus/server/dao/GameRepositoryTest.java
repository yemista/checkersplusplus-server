package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

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

import com.checklersplusplus.server.model.GameModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class GameRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Test
	public void canGetByGameId() {
		GameModel gameModel = new GameModel();
		gameModel.setActive(false);
		gameModel.setGameState("");
		gameRepository.saveAndFlush(gameModel);
		assertThat(gameModel.getGameId()).isNotNull();
		Optional<GameModel> fetchedGame = gameRepository.getByGameId(gameModel.getGameId());
		assertThat(fetchedGame).isPresent();
		assertThat(fetchedGame.get().getGameId()).isEqualTo(gameModel.getGameId());
	}
	
	@Test
	public void canGetByRedAccountId() {
		UUID redId = UUID.randomUUID();
		GameModel gameModel = new GameModel();
		gameModel.setActive(true);
		gameModel.setInProgress(true);
		gameModel.setGameState("");
		gameModel.setRedId(redId);
		gameRepository.saveAndFlush(gameModel);
		assertThat(gameModel.getGameId()).isNotNull();
		Optional<GameModel> fetchedGame = gameRepository.getActiveGameByAccountId(redId);
		assertThat(fetchedGame).isPresent();
		assertThat(fetchedGame.get().getGameId()).isEqualTo(gameModel.getGameId());
	}
	
	@Test
	public void canGetByBlackAccountId() {
		UUID blackId = UUID.randomUUID();
		GameModel gameModel = new GameModel();
		gameModel.setActive(true);
		gameModel.setInProgress(true);
		gameModel.setGameState("");
		gameModel.setRedId(blackId);
		gameRepository.saveAndFlush(gameModel);
		assertThat(gameModel.getGameId()).isNotNull();
		Optional<GameModel> fetchedGame = gameRepository.getActiveGameByAccountId(blackId);
		assertThat(fetchedGame).isPresent();
		assertThat(fetchedGame.get().getGameId()).isEqualTo(gameModel.getGameId());
	}
	
	@Test
	public void canGetOpenGames() {
		UUID blackId = UUID.randomUUID();
		GameModel gameModel = new GameModel();
		gameModel.setActive(true);
		gameModel.setInProgress(false);
		gameModel.setGameState("");
		gameModel.setRedId(blackId);
		gameRepository.saveAndFlush(gameModel);
		assertThat(gameModel.getGameId()).isNotNull();
		
		UUID redId = UUID.randomUUID();
		GameModel gameModel2 = new GameModel();
		gameModel2.setActive(true);
		gameModel2.setInProgress(false);
		gameModel2.setGameState("");
		gameModel2.setRedId(redId);
		gameRepository.saveAndFlush(gameModel2);
		assertThat(gameModel2.getGameId()).isNotNull();
		
		UUID thirdId = UUID.randomUUID();
		GameModel gameModel3 = new GameModel();
		gameModel3.setActive(true);
		gameModel3.setInProgress(true);
		gameModel3.setGameState("");
		gameModel3.setRedId(thirdId);
		gameRepository.saveAndFlush(gameModel3);
		assertThat(gameModel3.getGameId()).isNotNull();
		
		UUID fourthId = UUID.randomUUID();
		GameModel gameModel4 = new GameModel();
		gameModel4.setActive(false);
		gameModel4.setInProgress(false);
		gameModel4.setGameState("");
		gameModel4.setRedId(fourthId);
		gameRepository.saveAndFlush(gameModel4);
		assertThat(gameModel4.getGameId()).isNotNull();
		
		List<GameModel> fetchedGames = gameRepository.getOpenGames();
		assertThat(fetchedGames.size()).isEqualTo(2);
	}
}
