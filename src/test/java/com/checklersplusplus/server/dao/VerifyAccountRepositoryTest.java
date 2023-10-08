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

import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.VerifyAccountModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class VerifyAccountRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Test
	public void canInactivateForAccountId() {
		UUID accountId = UUID.randomUUID();
		VerifyAccountModel verifyAccount1 = new VerifyAccountModel();
		verifyAccount1.setActive(true);
		verifyAccount1.setAccountId(accountId);
		verifyAccountRepository.save(verifyAccount1);
		assertThat(verifyAccount1.getVerifyAccountId()).isNotNull();
		
		VerifyAccountModel verifyAccount2 = new VerifyAccountModel();
		verifyAccount2.setActive(true);
		verifyAccount2.setAccountId(accountId);
		verifyAccountRepository.save(verifyAccount2);
		assertThat(verifyAccount2.getVerifyAccountId()).isNotNull();
		
		VerifyAccountModel verifyAccount3 = new VerifyAccountModel();
		verifyAccount3.setActive(true);
		verifyAccount3.setAccountId(accountId);
		verifyAccountRepository.save(verifyAccount3);
		assertThat(verifyAccount3.getVerifyAccountId()).isNotNull();
		
		List<VerifyAccountModel> verifyAccounts = verifyAccountRepository.getByAccountId(accountId);
		assertThat(verifyAccounts.size()).isEqualTo(3);
		
		verifyAccountRepository.inactivateForAccountId(accountId);

		Optional<VerifyAccountModel> activeVerifyAccount = verifyAccountRepository.getActiveByAccountId(accountId);
		assertThat(activeVerifyAccount.isPresent()).isFalse();
	}
	
	@Test
	public void canGetActiveByUsername() {
		String username = "test";
		AccountModel account = new AccountModel();
		account.setEmail("test@test.com");
		account.setUsername(username);
		accountRepository.save(account);
		assertThat(account.getAccountId()).isNotNull();
		UUID accountId = account.getAccountId();
		
		VerifyAccountModel verifyAccount1 = new VerifyAccountModel();
		verifyAccount1.setActive(false);
		verifyAccount1.setAccountId(accountId);
		verifyAccountRepository.save(verifyAccount1);
		assertThat(verifyAccount1.getVerifyAccountId()).isNotNull();
		
		VerifyAccountModel verifyAccount2 = new VerifyAccountModel();
		verifyAccount2.setActive(true);
		verifyAccount2.setAccountId(accountId);
		verifyAccountRepository.save(verifyAccount2);
		assertThat(verifyAccount2.getVerifyAccountId()).isNotNull();
		
		Optional<VerifyAccountModel> fetchedVerifyAccount = verifyAccountRepository.getActiveByUsername(username);
		assertThat(fetchedVerifyAccount.isPresent()).isTrue();
		assertThat(fetchedVerifyAccount.get().getVerifyAccountId()).isEqualTo(verifyAccount2.getVerifyAccountId());
	}
	
	@Test
	public void canGetActiveByAccountId() {
		String username = "test";
		AccountModel account = new AccountModel();
		account.setEmail("test@test.com");
		account.setUsername(username);
		accountRepository.save(account);
		assertThat(account.getAccountId()).isNotNull();
		UUID accountId = account.getAccountId();
		
		VerifyAccountModel verifyAccount1 = new VerifyAccountModel();
		verifyAccount1.setActive(false);
		verifyAccount1.setAccountId(accountId);
		verifyAccountRepository.save(verifyAccount1);
		assertThat(verifyAccount1.getVerifyAccountId()).isNotNull();
		
		VerifyAccountModel verifyAccount2 = new VerifyAccountModel();
		verifyAccount2.setActive(true);
		verifyAccount2.setAccountId(accountId);
		verifyAccountRepository.save(verifyAccount2);
		assertThat(verifyAccount2.getVerifyAccountId()).isNotNull();
		
		Optional<VerifyAccountModel> fetchedVerifyAccount = verifyAccountRepository.getActiveByAccountId(accountId);
		assertThat(fetchedVerifyAccount.isPresent()).isTrue();
		assertThat(fetchedVerifyAccount.get().getVerifyAccountId()).isEqualTo(verifyAccount2.getVerifyAccountId());
	}
	
}
