package com.checklersplusplus.server.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checklersplusplus.server.model.BotModel;

public interface BotRepository extends JpaRepository<BotModel, UUID> {

	public Optional<BotModel> findByBotAccountId(UUID botAccountId);
	
	public Optional<BotModel> findFirstByInUseFalse();
	
	public List<BotModel> findByInUseTrue();
}
