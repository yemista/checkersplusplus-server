package com.checklersplusplus.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.RatingRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.internal.Rating;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.exception.CheckersPlusPlusServerException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.RatingModel;
import com.checklersplusplus.server.model.VerifyAccountModel;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RatingServiceTest {
	
	private static final String TEST_EMAIL = "test@test.com";
	private static final String TEST_USERNAME = "test";
	
	private static final String TEST_EMAIL_2 = "test2@test.com";
	private static final String TEST_USERNAME_2 = "test2";
	
	private static final String TEST_PASSWORD = "Password123";

	@Autowired
	private RatingService ratingService;
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private RatingRepository ratingRepository;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<GameModel> gamesToDelete = new ArrayList<>();
	private List<VerifyAccountModel> verifyAccountsToDelete = new ArrayList<>();
	
	@After
	public void cleanupDatabaseObjects() {
		accountsToDelete.forEach(account -> accountRepository.delete(account));
		verifyAccountsToDelete.forEach(verifyAccount -> verifyAccountRepository.delete(verifyAccount));
		gamesToDelete.forEach(game -> gameRepository.delete(game));
	}
	
	@Test
	public void testUpdateRatings() throws Exception {
		UUID account1 = createAndVerifyAccount(TEST_EMAIL, TEST_USERNAME);
		UUID account2 = createAndVerifyAccount(TEST_EMAIL_2, TEST_USERNAME_2);
		GameModel gameModel = new GameModel();
		gameModel.setActive(false);
		gameModel.setBlackId(account1);
		gameModel.setInProgress(false);
		gameModel.setRedId(account2);
		gameModel.setWinnerId(account2);
		gameRepository.save(gameModel);
		gamesToDelete.add(gameModel);
		Rating account1RatingPre = ratingService.getRatingForPlayer(account1);
		Rating account2RatingPre = ratingService.getRatingForPlayer(account2);
		assertThat(account1RatingPre.getRating()).isEqualTo(800);
		assertThat(account2RatingPre.getRating()).isEqualTo(800);
		ratingService.updatePlayerRatings(gameModel.getGameId());
		Rating account1RatingPost = ratingService.getRatingForPlayer(account1);
		Rating account2RatingPost = ratingService.getRatingForPlayer(account2);
		assertThat(account1RatingPost.getRating()).isEqualTo(784);
		assertThat(account2RatingPost.getRating()).isEqualTo(816);
	}
	
	@Test
	public void testUpdateRatingsGameNotFound() throws Exception {
		try {
			ratingService.updatePlayerRatings(UUID.randomUUID());
			fail();
		} catch (CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not found.");
		}
	}
	
	@Test
	public void testUpdateRatingsGameNotComplete() throws Exception {
		UUID account1 = createAndVerifyAccount(TEST_EMAIL, TEST_USERNAME);
		UUID account2 = createAndVerifyAccount(TEST_EMAIL_2, TEST_USERNAME_2);
		GameModel gameModel = new GameModel();
		gameModel.setActive(false);
		gameModel.setBlackId(account1);
		gameModel.setInProgress(false);
		gameModel.setRedId(account2);
		gameRepository.save(gameModel);
		gamesToDelete.add(gameModel);
		
		try {
			ratingService.updatePlayerRatings(gameModel.getGameId());
			fail();
		} catch (CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Game not complete");
		}
	}
	
	@Test
	public void testUpdateRatingsWinnerMissingRating() throws Exception {
		UUID account1 = createAndVerifyAccount(TEST_EMAIL, TEST_USERNAME);
		UUID account2 = createAndVerifyAccount(TEST_EMAIL_2, TEST_USERNAME_2);
		GameModel gameModel = new GameModel();
		gameModel.setActive(false);
		gameModel.setBlackId(account1);
		gameModel.setInProgress(false);
		gameModel.setRedId(account2);
		gameModel.setWinnerId(account2);
		gameRepository.save(gameModel);
		gamesToDelete.add(gameModel);
		Optional<RatingModel> ratingModel = ratingRepository.findByAccountId(account2);
		ratingRepository.delete(ratingModel.get());
		
		try {
			ratingService.updatePlayerRatings(gameModel.getGameId());
			fail();
		} catch (CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Illegal state. Missing rating");
		}
	}
	
	@Test
	public void testUpdateRatingsLoserMissingRating() throws Exception {
		UUID account1 = createAndVerifyAccount(TEST_EMAIL, TEST_USERNAME);
		UUID account2 = createAndVerifyAccount(TEST_EMAIL_2, TEST_USERNAME_2);
		GameModel gameModel = new GameModel();
		gameModel.setActive(false);
		gameModel.setBlackId(account1);
		gameModel.setInProgress(false);
		gameModel.setRedId(account2);
		gameModel.setWinnerId(account2);
		gameRepository.save(gameModel);
		gamesToDelete.add(gameModel);
		Optional<RatingModel> ratingModel = ratingRepository.findByAccountId(account1);
		ratingRepository.delete(ratingModel.get());
		
		try {
			ratingService.updatePlayerRatings(gameModel.getGameId());
			fail();
		} catch (CheckersPlusPlusServerException e) {
			assertThat(e.getMessage()).isEqualTo("Illegal state. Missing rating");
		}
	}
	
	private UUID createAndVerifyAccount(String email, String username) throws Exception {
		CreateAccount createAccountInput = new CreateAccount(email, TEST_PASSWORD, TEST_PASSWORD, username);
		AccountModel accountModel = createAccount(createAccountInput);
		accountModel.setVerified(LocalDateTime.now());
		accountRepository.save(accountModel);
		return accountModel.getAccountId();
	}
	
	private AccountModel createAccount(CreateAccount createAccount) throws Exception {
		accountService.createAccount(createAccount);
		Optional<AccountModel> accountModel = accountRepository.getByEmail(createAccount.getEmail());
		assertTrue(accountModel.isPresent());
		accountsToDelete.add(accountModel.get());
		Optional<VerifyAccountModel> verifyAccount = verifyAccountRepository.getActiveByAccountId(accountModel.get().getAccountId());
		assertTrue(verifyAccount.isPresent());
		verifyAccountsToDelete.add(verifyAccount.get());
		return accountModel.get();
	}
	
}
