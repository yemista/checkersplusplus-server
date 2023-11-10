package com.checklersplusplus.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.GameRepository;
import com.checklersplusplus.server.dao.SessionRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.entities.request.CreateAccount;
import com.checklersplusplus.server.entities.response.GameHistory;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.GameModel;
import com.checklersplusplus.server.model.SessionModel;
import com.checklersplusplus.server.model.VerifyAccountModel;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GameHistoryServiceTest {
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Autowired
	private GameRepository gameRepository;
	
	@Autowired
	private SessionRepository sessionRepository;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;

	@Autowired
	private GameHistoryService gameHistoryService;
	
	private List<AccountModel> accountsToDelete = new ArrayList<>();
	private List<VerifyAccountModel> verifyAccountsToDelete = new ArrayList<>();
	private List<GameModel> gamesToDelete = new ArrayList<>();
	
	private UUID sessionId;
	private GameModel game1;
	private GameModel game2;
	private GameModel game3;
	
	@Before
	public void setup() throws Exception {
		CreateAccount createAccountInput1 = new CreateAccount("test@test.com", "Password123", "Password123", "test");
		AccountModel account1 = createAccount(createAccountInput1);
		
		CreateAccount createAccountInput2 = new CreateAccount("test2@test.com", "Password123", "Password123", "test2");
		AccountModel account2 = createAccount(createAccountInput2);
		SessionModel session = new SessionModel();
		session.setAccountId(account2.getAccountId());
		session.setActive(true);
		session.setLastModified(LocalDateTime.now());
		sessionRepository.save(session);
		sessionId = session.getSessionId();
		
		game1 = createGame(account2.getAccountId(), account1.getAccountId());
		game2 = createGame(account1.getAccountId(), account2.getAccountId());
		game3 = createGame(account2.getAccountId(), account1.getAccountId());
		createGame(UUID.randomUUID(), account1.getAccountId());
	}
	
	@After
	public void cleanupDatabaseObjects() {
		accountsToDelete.forEach(account -> accountRepository.delete(account));
		verifyAccountsToDelete.forEach(verifyAccount -> verifyAccountRepository.delete(verifyAccount));
		gamesToDelete.forEach(game -> gameRepository.delete(game));
	}
	
	@Test
	public void canSortByLastModifiedDesc() throws Exception {
		List<GameHistory> games = gameHistoryService.getGameHistory(sessionId, "desc", 0, 5);
		assertThat(games.size()).isEqualTo(3);
		assertThat(games.get(0).getGameId()).isEqualTo(game3.getGameId());
		assertThat(games.get(2).getGameId()).isEqualTo(game1.getGameId());
	}
	
	@Test
	public void canSortByLastModifiedAsc() throws Exception {
		List<GameHistory> games = gameHistoryService.getGameHistory(sessionId, "asc", 0, 5);
		assertThat(games.size()).isEqualTo(3);
		assertThat(games.get(0).getGameId()).isEqualTo(game1.getGameId());
		assertThat(games.get(2).getGameId()).isEqualTo(game3.getGameId());
	}
	
	private GameModel createGame(UUID blackAccountId, UUID redAccountId) {
		GameModel game1 = new GameModel();
		game1.setActive(false);
		game1.setBlackId(blackAccountId);
		game1.setBlackRating(800);
		game1.setCreated(LocalDate.now());
		game1.setCreatorRating(800);
		game1.setCurrentMoveNumber(0);
		game1.setGameState("");
		game1.setInProgress(false);
		game1.setLastModified(LocalDateTime.now());
		game1.setRedId(redAccountId);
		game1.setRedRating(800);
		game1.setWinnerId(redAccountId);
		gameRepository.save(game1);
		gamesToDelete.add(game1);
		return game1;
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
