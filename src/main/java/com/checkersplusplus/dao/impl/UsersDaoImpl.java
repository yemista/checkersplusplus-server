package com.checkersplusplus.dao.impl;


import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.checkersplusplus.dao.UsersDao;
import com.checkersplusplus.dao.models.UserModel;

@Repository
@Transactional
@Component
public class UsersDaoImpl implements UsersDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public UserModel getUserByEmail(String email) {
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<UserModel> query = builder.createQuery(UserModel.class);
		Root<UserModel> root = query.from(UserModel.class);
		query.select(root).where(builder.equal(root.get("email"), email));
		Query<UserModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<UserModel> listResult = q.getResultList();
		return listResult.isEmpty() ? null : listResult.get(0);
	}

	@Override
	public boolean isAliasInUse(String alias) {
		sessionFactory.getCurrentSession().createNativeQuery("SELECT user_id FROM users where alias)
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<UserModel> query = builder.createQuery(UserModel.class);
		Root<UserModel> root = query.from(UserModel.class);
		query.select(root).where(builder.equal(root.get("alias"), alias));
		Query<UserModel> q = sessionFactory.getCurrentSession().createQuery(query);
		return !q.getResultList().isEmpty();
	}

	@Override
	public boolean isEmailInUse(String email) {
		return getUserByEmail(email) != null;
	}

	@Override
	public void createUser(String email, String password, String alias) {
		UserModel user = new UserModel();
		user.setId(UUID.randomUUID());
		user.setEmail(email);
		user.setPassword(password);
		user.setAlias(alias);
		user.setCreateDate(new Date());
		sessionFactory.getCurrentSession().save(user);
	}
 
}
