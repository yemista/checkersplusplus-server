package com.checklersplusplus.server.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.checklersplusplus.server.dao.AccountRepository;
import com.checklersplusplus.server.dao.VerifyAccountRepository;
import com.checklersplusplus.server.exception.InvalidVerificationCodeException;
import com.checklersplusplus.server.exception.UsernameNotFoundException;
import com.checklersplusplus.server.model.AccountModel;
import com.checklersplusplus.server.model.VerifyAccountModel;
import com.checklersplusplus.server.util.VerificationCodeUtil;

@Service
@Transactional
public class VerificationService {
	
	private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);
	
	@Autowired
	private VerifyAccountRepository verifyAccountRepository;
	
	@Autowired
	private AccountRepository accountRepository;

	public String createVerificationCode(UUID accountId) {
		verifyAccountRepository.inactivateForAccountId(accountId);
		VerifyAccountModel verifyAccountModel = new VerifyAccountModel();
		verifyAccountModel.setAccountId(accountId);
		verifyAccountModel.setCreated(LocalDateTime.now());
		String verificationCode = VerificationCodeUtil.generateVerificationCode();
		verifyAccountModel.setVerificationCode(verificationCode);
		verifyAccountModel.setActive(true);
		verifyAccountRepository.save(verifyAccountModel);
		return verificationCode;
	}
	
	public void verifyAccount(String username, String verificationCode) throws Exception {
		Optional<AccountModel> accountModelOptional = accountRepository.getByUsername(username);
		
		if (accountModelOptional.isEmpty()) {
			throw new UsernameNotFoundException();
		}
		
		AccountModel accountModel = accountModelOptional.get();		
		Optional<VerifyAccountModel> verifyAccountModelOptional = verifyAccountRepository.getActiveByAccountId(accountModel.getAccountId());
		
		if (verifyAccountModelOptional.isEmpty() || !verificationCode.equals(verifyAccountModelOptional.get().getVerificationCode())) {
			throw new InvalidVerificationCodeException();
		}
		
		VerifyAccountModel verifyAccountModel = verifyAccountModelOptional.get();
		verifyAccountModel.setActive(false);
		verifyAccountRepository.save(verifyAccountModel);
		accountModel.setVerified(LocalDateTime.now());
		accountRepository.save(accountModel);
		logger.debug(String.format("Account verified by %s", username));
	}

}
