package com.checklersplusplus.server.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.checklersplusplus.server.model.AccountModel;

@RunWith(SpringRunner.class)
@DataJpaTest
public class AccountRepositoryTest {

	@Autowired
	private TestEntityManager entityManager;
	
	@Autowired
	private AccountRepository accountRepository;
	
	@Test
	public void cannotFindFromEmptyRepository() {
		AccountModel account = new AccountModel();
		account.setUsername("test");
		account.setEmail("test@test.com");
		account.setPassword("1234567890");
		accountRepository.saveAndFlush(account);
		assertThat(account.getAccountId()).isNotNull();
		AccountModel account2 = new AccountModel();
		account2.setUsername("test");
		account2.setEmail("test2@test.com");
		account2.setPassword("1234567890");
		accountRepository.saveAndFlush(account2);
		assertThat(account.getAccountId()).isNotEqualTo(account2.getAccountId());
	}
}

