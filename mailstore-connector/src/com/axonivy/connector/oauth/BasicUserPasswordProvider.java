package com.axonivy.connector.oauth;

import com.axonivy.connector.mailstore.MailStoreService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

/**
 * The {@code BasicUserPasswordProvider} class implements the 
 * {@link UserPasswordProvider} interface, providing a simple 
 * implementation for retrieving user credentials from a mail store.
 * <p>
 * This class provides methods to obtain both the username and password 
 * for a specified store name, utilizing the {@link MailStoreService}.
 * </p>
 */
public class BasicUserPasswordProvider implements UserPasswordProvider {
    private static final Logger LOG = Ivy.log();

    /**
     * Retrieves the username associated with the specified store name.
     *
     * @param storeName the name of the store
     * @return the username for the specified store
     */
	@Override
	public String getUser(String storeName) {
		LOG.debug("Retrieving user for store: ''{0}''.", storeName);
		
		return MailStoreService.getVar(storeName, USER_VAR);
	}

    /**
     * Retrieves the password associated with the specified store name.
     *
     * @param storeName the name of the store
     * @return the password for the specified store
     */
	@Override
	public String getPassword(String storeName) {
		LOG.debug("Retrieving password for store: ''{0}''.", storeName);
		
		return MailStoreService.getVar(storeName, PASSWORD_VAR);
	}
	
}
