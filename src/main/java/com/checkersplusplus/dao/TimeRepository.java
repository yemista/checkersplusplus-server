package com.checkersplusplus.dao;

import java.util.Date;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TimeRepository {

	@Autowired
	private SessionFactory sessionFactory;
	
	public Date getCurrentTimestamp() {
		return (Date) sessionFactory.getCurrentSession().createQuery("SELECT current_timestamp()"); 
	}
}
