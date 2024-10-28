package com.axonivy.connector.oauth;

public interface UserPasswordProvider {
	 String getUser(String storeName);
	 String getPassword(String storeName);
}
