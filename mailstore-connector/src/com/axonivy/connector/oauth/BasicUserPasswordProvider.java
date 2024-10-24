package com.axonivy.connector.oauth;

import com.axonivy.connector.mailstore.MailStoreService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

public class BasicUserPasswordProvider implements UserPasswordProvider {
    private static final Logger LOG = Ivy.log();
	private static final String PASSWORD_VAR = "password";

	@Override
	public String authenticate(String storeName) {
        LOG.debug("Connect to store {0} using Basic Authentication.", storeName);
		
		return MailStoreService.getVar(storeName, PASSWORD_VAR);
	}
	
}
