package com.checkersplusplus.dao;

import org.springframework.data.repository.CrudRepository;

import com.checkersplusplus.dao.models.GameModel;

public interface GameRepository extends CrudRepository<GameModel, String> {

}
