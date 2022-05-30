package com.checkersplusplus.service;

import com.checkersplusplus.service.models.Session;

public interface SessionService {

	String createUserSession(String userId);
	
	Session getSessionByTokenId(String tokenId);

	Session getLatestActiveSessionByUserId(String userId);
}
