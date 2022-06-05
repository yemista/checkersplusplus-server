package com.checkersplusplus.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.models.SessionModel;

@Transactional
public interface SessionRepository extends CrudRepository<SessionModel, Long> {

	@Query("SELECT s FROM SessionModel s WHERE s.token = :token AND s.active = true ORDER BY s.createDate DESC")
	List<SessionModel> getSessionByToken(String token);

	@Query("SELECT s FROM SessionModel s WHERE s.userId = :userId ORDER BY s.createDate DESC")
	List<SessionModel> getLatestActiveSessionByUserId(@Param("userId") String userId);
	
	@Modifying
	@Query("UPDATE SessionModel s SET s.active = false WHERE s.userId = :userId AND s.active = true")
	int invalidateExistingSessions(@Param("userId") String userId);
}
