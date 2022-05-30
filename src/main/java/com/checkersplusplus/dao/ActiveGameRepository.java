package com.checkersplusplus.dao;

import org.springframework.data.repository.CrudRepository;

import com.checkersplusplus.dao.models.ActiveGameModel;

public interface ActiveGameRepository extends CrudRepository<ActiveGameModel, Long> {

}
