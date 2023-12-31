package com.checklersplusplus.server.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checklersplusplus.server.model.BotModel;

public interface BotRepository extends JpaRepository<BotModel, UUID> {

	//public List<BotModel> getAllBots();
}
