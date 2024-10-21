package com.axonivy.connector.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenDTO {
	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("expires_in")
	private int expiresIn;

	@JsonProperty("ext_expires_in")
	private int extExpiresIn;

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public int getExtExpiresIn() {
		return extExpiresIn;
	}

	public void setExtExpiresIn(int extExpiresIn) {
		this.extExpiresIn = extExpiresIn;
	}

}
