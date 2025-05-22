package com.axonivy.connector.mailstore.provider.oauth;

import java.util.Map;
import java.util.Optional;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.mailstore.MailStoreService;
import com.axonivy.connector.mailstore.provider.UserPasswordProvider;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

/**
 * The {@code AzureOauth2UserPasswordProvider} class implements the 
 * {@link UserPasswordProvider} interface, providing methods to retrieve 
 * user credentials specifically for Azure OAuth2 authentication.
 * <p>
 * This class retrieves user credentials from a mail store and handles 
 * interactions with Azure's OAuth2 token endpoint.
 * </p>
 */
public class AzureOauth2UserPasswordProvider implements UserPasswordProvider {
	private static final Logger LOG = Ivy.log();
	private static final String TENANT_ID = "tenantId";
	private static final String APP_ID = "appId";
	private static final String SECRET_KEY = "secretKey";
	private static final String GRANT_TYPE = "grantType";
	private static final String SCOPE = "scope";
	
	private static final String REST_CLIENT = "getTokenAzureOAuth";
    private static final String TOKEN_PATH = "oauth2/v2.0/token";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String CONTENT_TYPE_VALUE = "application/x-www-form-urlencoded";
	
    /**
     * The {@code FormProperty} interface defines constants for form property 
     * names used in the OAuth2 token request.
     */
	public static interface FormProperty {
		String CLIENT_ID = "client_id";
		String CLIENT_SECRET = "client_secret";
		String SCOPE = "scope";
		String GRANT_TYPE = "grant_type";
		String USERNAME = "username";
		String PASSWORD = "password";
	}
    
	public static interface ResponseProperty {
		String ACCESS_TOKEN = "access_token";
	}
	
    /**
     * Retrieves the username associated with the specified store name.
     *
     * @param storeName the name of the store
     * @return the username for the specified store
     */
	@Override
	public String getUser(String storeName) {
		LOG.debug("[AzureOauth2UserPasswordProvider] Retrieving user for store: ''{0}''.", storeName);
		
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
		LOG.debug("[AzureOauth2UserPasswordProvider] Retrieving password for store: ''{0}''.", storeName);

		return getToken(storeName);
	}
	
	private Form buildForm(String storeName) {
		Form form = new Form();
		form.param(FormProperty.CLIENT_ID, MailStoreService.getVar(storeName, APP_ID));
		form.param(FormProperty.CLIENT_SECRET, MailStoreService.getVar(storeName, SECRET_KEY));
		form.param(FormProperty.SCOPE, MailStoreService.getVar(storeName, SCOPE));
		
		String grantTypeValue = MailStoreService.getVar(storeName, GRANT_TYPE);
		
		LOG.debug("[AzureOauth2UserPasswordProvider] Grant type value retrieved for store {0}: {1}", storeName, grantTypeValue);
		
		form.param(FormProperty.GRANT_TYPE, grantTypeValue);

		if (GrantType.isUserPassAuth(grantTypeValue)) {
			form.param(FormProperty.USERNAME, getUser(storeName));
			form.param(FormProperty.PASSWORD, MailStoreService.getVar(storeName, PASSWORD_VAR));
		}

		return form;
	}
	
	private Response sendTokenRequest(String tenantId, Form form) {
		return Ivy.rest()
				.client(REST_CLIENT)
				.path(tenantId)
				.path(TOKEN_PATH).request()
				.header(CONTENT_TYPE_HEADER, CONTENT_TYPE_VALUE)
				.post(Entity.form(form));
	}
	
	private String getToken(String storeName) {
		Form form = buildForm(storeName);
		String tenantId = MailStoreService.getVar(storeName, TENANT_ID);

		Response response = sendTokenRequest(tenantId, form);

		if (null == response) {
			final String nullResponseMessage = "[AzureOauth2UserPasswordProvider] response cannot be null";
			LOG.error(nullResponseMessage);
			throw MailStoreService.buildError("getToken").withMessage(nullResponseMessage).build();
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
		
		return Optional.ofNullable(values).map(value -> values.get(ResponseProperty.ACCESS_TOKEN).toString()).orElse(null); 
	}
	
	private static enum GrantType {
	    APPLICATION("client_credentials"),

	    /** weak security: app acts as pre-configured personal user! **/
	    PASSWORD("password");

	    private String type;

	    GrantType(String type) {
	      this.type = type;
	    }

	    private static boolean isUserPassAuth(String str) {
	        return str != null && (str.equals(PASSWORD.type));
	    }
	    
	  }

}