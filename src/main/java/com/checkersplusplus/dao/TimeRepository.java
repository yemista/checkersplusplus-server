package com.checkersplusplus.dao;

import java.util.Date;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import com.checkersplusplus.dao.models.DateItem;

@Repository
public class TimeRepository {
	
	@PersistenceContext
	private EntityManager em;
	
	public Date getCurrentTimestamp() {
	    Query query = em.createNativeQuery("SELECT CURRENT_TIMESTAMP AS DATE_VALUE", DateItem.class);
	    DateItem dateItem = (DateItem) query.getSingleResult();
	    return dateItem.getDate();
	}
}
