package com.checklersplusplus.server.dao;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.checklersplusplus.server.model.GameMoveModel;

public interface GameMoveRepository extends JpaRepository<GameMoveModel, UUID> {

	public Optional<GameMoveModel> findFirstByGameIdOrderByMoveNumberDesc(UUID gameId);

}
