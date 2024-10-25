package com.axonivy.connector.oauth;

import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.mailstore.MailStoreService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

public class AzureOauth2UserPasswordProvider implements UserPasswordProvider {
	private static final Logger LOG = Ivy.log();
	private static final String TENANT_ID = "tenantId";
	private static final String APP_ID = "appId";
	private static final String SECRET_KEY = "secretKey";
	private static final String GRANT_TYPE = "grantType";
	private static final String SCOPE = "scope";
	
	private static final String REST_CLIENT = "getTokenAzureOAuth";
	
	@Override
	public String authenticate(String storeName) {
		LOG.debug("Connect to store {0} using OAuth2 Authentication.", storeName);

		return getToken(storeName);
	}
	
	private Form buildForm(String storeName) {
		Form form = new Form();
		form.param("client_id", MailStoreService.getVar(storeName, APP_ID));
		form.param("client_secret", MailStoreService.getVar(storeName, SECRET_KEY));
		form.param("scope", MailStoreService.getVar(storeName, SCOPE));
		form.param("grant_type", MailStoreService.getVar(storeName, GRANT_TYPE));

		return form;
	}
	
	private Response sendTokenRequest(String tenantId, Form form) {
	    return Ivy.rest().client(REST_CLIENT)
	        .path(tenantId)
	        .path("oauth2/v2.0/token")
	        .request()
	        .header("Content-Type", "application/x-www-form-urlencoded")
	        .post(Entity.form(form));
	}
	
	private String getToken(String storeName) {
		Form form = buildForm(storeName);
		String tenantId = MailStoreService.getVar(storeName, TENANT_ID);

		Response response = sendTokenRequest(tenantId, form);

		if (null == response) {
			LOG.error("response cannot be null");
			throw MailStoreService.buildError("getToken").withMessage("response cannot be null").build();
		}

		String accessToken = extractToken(response);

		if (StringUtils.isEmpty(accessToken)) {
			throw new IllegalStateException("Failed to read 'access_token' from " + response);
		}

		return accessToken;
	}

	// get response entity from response
	private String extractToken(Response response) {
		GenericType<Map<String, Object>> map = new GenericType<>(Map.class);
		Map<String, Object> values = response.readEntity(map);
		
		if(null == values) {
			return null;
		}

		return values.get("access_token").toString();
	}
}