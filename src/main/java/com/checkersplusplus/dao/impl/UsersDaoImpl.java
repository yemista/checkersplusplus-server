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

import com.checkersplusplus.crypto.PasswordCryptoUtil;
import com.checkersplusplus.dao.UsersDao;
import com.checkersplusplus.dao.models.UserModel;
import com.checkersplusplus.service.models.User;

@Repository
@Transactional
@Component
public class UsersDaoImpl implements UsersDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public User getUserByEmail(String email) {
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<UserModel> query = builder.createQuery(UserModel.class);
		Root<UserModel> root = query.from(UserModel.class);
		query.select(root).where(builder.equal(root.get("email"), email));
		Query<UserModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<UserModel> listResult = q.getResultList();
		UserModel userModel = listResult.isEmpty() ? null : listResult.get(0);
		return userModel == null ? null : new User(userModel.getId(), userModel.getEmail(), userModel.getPassword(), userModel.getAlias());
	}

	@Override
	public boolean isAliasInUse(String alias) {
		CriteriaBuilder builder = sessionFactory.getCriteriaBuilder();
		CriteriaQuery<UserModel> query = builder.createQuery(UserModel.class);
		Root<UserModel> root = query.from(UserModel.class);
		query.select(root).where(builder.equal(root.get("alias"), alias));
		Query<UserModel> q = sessionFactory.getCurrentSession().createQuery(query);
		List<UserModel> listResult = q.getResultList();
		return listResult.isEmpty() ? false : true;
	}

	@Override
	public boolean isEmailInUse(String email) {
		return getUserByEmail(email) != null;
	}

	@Override
	public void createUser(String email, String password, String alias) {
		UserModel user = new UserModel();
		user.setId(UUID.randomUUID().toString());
		user.setEmail(email);
		user.setPassword(PasswordCryptoUtil.encryptPasswordForDatabase(password));
		user.setAlias(alias);
		user.setCreateDate(new Date());
		sessionFactory.getCurrentSession().persist(user);
	}
 
}
