package com.checkersplusplus.dao.models;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "users")
public class UserModel {

	@Id
	@Column(name = "user_id", updatable = false, nullable = false)
    private UUID id;
	
	@Column(name = "email", updatable = false, nullable = false, unique = true)
	private String email;
	
	@Column(name = "password", updatable = true, nullable = false)
	private String password;
	
	@Column(name = "alias", updatable = false, nullable = false, unique = true)
	private String alias;
	
	@Column(name = "created", updatable = false, nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
    private Date createDate;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
}

