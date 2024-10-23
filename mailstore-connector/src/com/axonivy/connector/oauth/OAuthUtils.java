package com.axonivy.connector.oauth;

import java.util.Map;

import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;


public class OAuthUtils {
	
	// get response entity from response
	public static TokenDTO extractToken(Response response) {
		GenericType<Map<String, Object>> map = new GenericType<>(Map.class);
		Map<String, Object> values = response.readEntity(map);

		TokenDTO tokenDto = new TokenDTO();
		tokenDto.setAccessToken(values.get("access_token").toString());

		return tokenDto;
	}


}
