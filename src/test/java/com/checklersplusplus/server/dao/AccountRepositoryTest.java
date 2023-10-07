package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.model.AccountModel;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(locations="classpath:h2.properties")
public class AccountRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Test
	public void canGetByUsername() {
		AccountModel account = new AccountModel();
		account.setUsername("test");
		account.setEmail("test@test.com");
		account.setPassword("1234567890");
		accountRepository.saveAndFlush(account);
		assertThat(account.getAccountId()).isNotNull();
		Optional<AccountModel> fetchedAccount = accountRepository.getByUsername("test");
		assertThat(fetchedAccount).isPresent();
		assertThat(fetchedAccount.get().getAccountId()).isEqualTo(fetchedAccount.get().getAccountId());
	}
	
	@Test
	public void canGetByEmail() {
		AccountModel account = new AccountModel();
		account.setUsername("test");
		account.setEmail("test@test.com");
		account.setPassword("1234567890");
		accountRepository.saveAndFlush(account);
		assertThat(account.getAccountId()).isNotNull();
		Optional<AccountModel> fetchedAccount = accountRepository.getByEmail("test@test.com");
		assertThat(fetchedAccount).isPresent();
		assertThat(fetchedAccount.get().getAccountId()).isEqualTo(fetchedAccount.get().getAccountId());
	}
	
	@Test
	public void canFindByUsernameAndPassword() {
		AccountModel account = new AccountModel();
		account.setUsername("test");
		account.setEmail("test@test.com");
		account.setPassword("1234567890");
		accountRepository.saveAndFlush(account);
		assertThat(account.getAccountId()).isNotNull();
		Optional<AccountModel> fetchedAccount = accountRepository.findByUsernameAndPassword("test", "1234567890");
		assertThat(fetchedAccount).isPresent();
		assertThat(fetchedAccount.get().getAccountId()).isEqualTo(fetchedAccount.get().getAccountId());
	}
}

