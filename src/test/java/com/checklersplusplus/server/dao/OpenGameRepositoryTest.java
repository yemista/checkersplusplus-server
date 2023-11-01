package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.model.GameModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class OpenGameRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private OpenGameRepository openGameRepository;
	
	@Test
	public void canGetOpenGamesWithRatingFilter() {
		createGameWithBlackRating(800);
		createGameWithRedRating(700);
		createGameWithBlackRating(600);
		Page<GameModel> openGames = openGameRepository.findByCreatorRatingBetweenAndActiveTrue(700, 850, PageRequest.of(0, 50));
		assertThat(openGames.getContent().size()).isEqualTo(2);
	}
	
	@Test
	public void canSortOpenGamesWithRating() {
		PageRequest pageRequest = PageRequest.of(0, 2, Sort.by("creatorRating").descending());
		
		createGameWithRedRating(800);
		createGameWithBlackRating(800);
		createGameWithRedRatingInactive(700);
		createGameWithBlackRatingInactive(700);
		createGameWithRedRating(600);
		createGameWithBlackRating(600);
		
		Page<GameModel> openGames = openGameRepository.findByCreatorRatingBetweenAndActiveTrue(0, 750, pageRequest);
		assertThat(openGames.getContent().size()).isEqualTo(2);
		
		int rating1 = openGames.getContent().get(0).getBlackRating() == null ? openGames.getContent().get(0).getRedRating() :  openGames.getContent().get(0).getBlackRating();
		int rating2 = openGames.getContent().get(0).getBlackRating() == null ? openGames.getContent().get(0).getRedRating() :  openGames.getContent().get(0).getBlackRating();
		assertThat(rating1).isEqualTo(600);
		assertThat(rating2).isEqualTo(600);
	}
	
	private void createGameWithBlackRating(int rating) {
		GameModel game = new GameModel();
		game.setActive(true);
		game.setBlackId(UUID.randomUUID());
		game.setCreatorRating(rating);
		game.setCreated(LocalDateTime.now());
		game.setCurrentMoveNumber(0);
		game.setBlackRating(rating);
		gameRepository.save(game);
	}
	
	private void createGameWithRedRating(int rating) {
		GameModel game = new GameModel();
		game.setActive(true);
		game.setRedId(UUID.randomUUID());
		game.setCreatorRating(rating);
		game.setCreated(LocalDateTime.now());
		game.setCurrentMoveNumber(0);
		game.setRedRating(rating);
		gameRepository.save(game);
	}
	
	private void createGameWithBlackRatingInactive(int rating) {
		GameModel game = new GameModel();
		game.setActive(false);
		game.setBlackId(UUID.randomUUID());
		game.setCreatorRating(rating);
		game.setCreated(LocalDateTime.now());
		game.setCurrentMoveNumber(0);
		game.setBlackRating(rating);
		gameRepository.save(game);
	}
	
	private void createGameWithRedRatingInactive(int rating) {
		GameModel game = new GameModel();
		game.setActive(false);
		game.setRedId(UUID.randomUUID());
		game.setCreatorRating(rating);
		game.setCreated(LocalDateTime.now());
		game.setCurrentMoveNumber(0);
		game.setRedRating(rating);
		gameRepository.save(game);
	}
}
