package com.checklersplusplus.server.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.OpenWebSocketModel;

public interface OpenWebSocketRepository extends JpaRepository<OpenWebSocketModel, UUID> {

	@Modifying
	@Query("UPDATE OpenWebSocketModel SET active = false WHERE webSocketId = ?1 AND active = true")
	public void inactivateBySessionId(String sessionId);
}
