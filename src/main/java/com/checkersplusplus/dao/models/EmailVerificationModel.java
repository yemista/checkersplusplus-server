package com.checkersplusplus.dao.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "email_verification")
public class EmailVerificationModel {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "email_verification_id", updatable = false, nullable = false)
    private Long id;
	
	@Column(name = "email", updatable = false, nullable = false)
	private String email;
	
	@Column(name = "code", updatable = false, nullable = false)
	private String code;
	
	@Column(name = "active", updatable = true, nullable = false)
	private int active;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getActive() {
		return active;
	}

	public void setActive(int active) {
		this.active = active;
	}
	
	
}
