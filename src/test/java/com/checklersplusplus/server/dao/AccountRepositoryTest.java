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
		Optional<AccountModel> fetchedAccount = accountRepository.getByUsernameIgnoreCase("test");
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
	
	@Test
	public void testInvalidCharacters() {
		assertThat(invalidCharacters("")).isTrue();
		assertThat(invalidCharacters("abc 123")).isTrue();
		assertThat(invalidCharacters("abc[]123")).isTrue();
		assertThat(invalidCharacters("abc_123")).isFalse();
		assertThat(invalidCharacters("ABC_123")).isFalse();
	}
	
	private boolean invalidCharacters(String username) {
		if (username.length() < 3 || username.length() > 20) {
			return true;
		}
		
		for (int i = 0; i < username.length(); ++i) {
			char ch = username.charAt(i);
			
			if (ch == 95) {
				continue;
			}
			
			if (ch >= 48 && ch <= 57) {
				continue;
			}
			
			if (ch >= 65 && ch <= 90) {
				continue;
			}
			
			if (ch >= 97 && ch <= 122) {
				continue;
			}
			
			return true;
		}
		
		return false;
	}
}

