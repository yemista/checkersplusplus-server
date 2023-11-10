package com.checklersplusplus.server.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.checklersplusplus.server.model.OpenWebSocketModel;

public interface OpenWebSocketRepository extends JpaRepository<OpenWebSocketModel, UUID> {

	// TODO test
	@Modifying
	@Query("UPDATE OpenWebSocketModel SET active = false WHERE webSocketId = :webSocketId")
	public void inactivateByWebSocketId(String webSocketId);

	// TODO test
	@Query("SELECT o FROM OpenWebSocketModel o WHERE active = true AND sessionId = ?1")
	public Optional<OpenWebSocketModel> getActiveByServerSessionId(UUID serverSessionId);
	
	// TODO test
	@Query("SELECT o FROM OpenWebSocketModel o WHERE active = true AND serverId = ?1")
	public List<OpenWebSocketModel> getActiveByServerId(UUID serverId);
	
	@Query("SELECT o FROM OpenWebSocketModel o WHERE serverId = ?1")
	public List<OpenWebSocketModel> getAllByServerId(UUID serverId);
	
	public Optional<OpenWebSocketModel> findByWebSocketId(String webSocketId);
}
