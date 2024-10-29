package com.axonivy.connector.oauth;

public interface UserPasswordProvider {
	String USER_VAR = "user";
	String PASSWORD_VAR = "password";
	
	 String getUser(String storeName);
	 String getPassword(String storeName);
}
