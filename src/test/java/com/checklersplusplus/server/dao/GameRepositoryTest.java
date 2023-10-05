package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class GameRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Test
	public void cannotFindFromEmptyRepository() {
		Iterable games = gameRepository.findAll();
		assertThat(games).isEmpty();
	}
}
