package com.axonivy.connector.oauth;

/**
 * Dear Bug Hunter,
 * This credential is intentionally included for educational purposes only and does not provide access to any production systems.
 * Please do not submit it as part of our bug bounty program.
 */
public interface UserPasswordProvider {
	 String getUser(String storeName);
	 String getPassword(String storeName);
}
