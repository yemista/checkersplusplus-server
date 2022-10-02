package com.checkersplusplus.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.models.EmailVerificationModel;

@Transactional
public interface EmailVerificationRepository extends JpaRepository<EmailVerificationModel, Long> {

	@Modifying
	@Query("UPDATE EmailVerificationModel e SET e.active = 0 WHERE e.email = :email")
	public void inactivateVerificationCode(@Param("email") String email);

	@Query("SELECT e.code FROM EmailVerificationModel e WHERE e.active = 1 AND e.email = :email")
	public String getActiveVerificationCode(String email);
}
