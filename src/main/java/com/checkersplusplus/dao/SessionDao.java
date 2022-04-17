package com.checkersplusplus.dao;

import com.checkersplusplus.service.models.Session;

public interface SessionDao {

	String createUserSession(String userId);
	
	Session getSessionByTokenId(String tokenId);

}
