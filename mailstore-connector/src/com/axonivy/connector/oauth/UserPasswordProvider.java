package com.axonivy.connector.oauth;

/**
 * The {@code UserPasswordProvider} interface provides methods to retrieve 
 * user credentials such as username and password for a given store.
 * <p>
 * Implementing classes should define the behavior of how user credentials 
 * are retrieved based on the store name.
 * </p>
 */
public interface UserPasswordProvider {
	
	/** The variable name for the user. */
	String USER_VAR = "user";
	
	/** The variable name for the password. */
	String PASSWORD_VAR = "password";

	  /**
     * Retrieves the username associated with the specified store name.
     *
     * @param storeName the name of the store
     * @return the username for the specified store
     */
	String getUser(String storeName);

    /**
     * Retrieves the password associated with the specified store name.
     *
     * @param storeName the name of the store
     * @return the password for the specified store
     */
	String getPassword(String storeName);
}
