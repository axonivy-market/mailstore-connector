package com.axonivy.connector.oauth;

import com.axonivy.connector.mailstore.MailStoreService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

public class BasicUserPasswordProvider implements UserPasswordProvider {
    private static final Logger LOG = Ivy.log();
	private static final String PASSWORD_VAR = "password";
	private static final String USER_VAR = "user";

	@Override
	public String getUser(String storeName) {
		LOG.debug("Retrieving user for store: ''{0}''.", storeName);
		
		return MailStoreService.getVar(storeName, USER_VAR);
	}

	@Override
	public String getPassword(String storeName) {
		LOG.debug("Retrieving password for store: ''{0}''.", storeName);
		
		return MailStoreService.getVar(storeName, PASSWORD_VAR);
	}
	
}
