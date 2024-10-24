package com.axonivy.connector.oauth;

import java.util.Optional;

import javax.ws.rs.core.Form;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.connector.mailstore.MailStoreService;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.call.SubProcessCall;
import ch.ivyteam.ivy.process.call.SubProcessCallResult;
import ch.ivyteam.log.Logger;

public class AzureOauth2UserPasswordProvider implements UserPasswordProvider {
	private static final Logger LOG = Ivy.log();
	private static final String TENANT_ID = "tenantId";
	private static final String APP_ID = "appId";
	private static final String SECRET_KEY = "secretKey";
	private static final String GRANT_TYPE = "grantType";
	private static final String SCOPE = "scope";
	
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
	
	private String getToken(String storeName) {
		Form form = buildForm(storeName);

		String tenantId = MailStoreService.getVar(storeName, TENANT_ID);
		String tokenUrlPrefix = MailStoreService.getVar(storeName, "tokenUrl.tokenUrlPrefix");
		String tokenUrlSuffix = String.format(MailStoreService.getVar(storeName, "tokenUrl.tokenUrlSuffix"), tenantId);

		if (StringUtils.isBlank(tokenUrlPrefix) || StringUtils.isBlank(tokenUrlSuffix)) {
			LOG.error("url to get token cannot be null or empty");
			throw MailStoreService.buildError("getToken").withMessage("url to get token cannot be null or empty")
					.build();
		}

		TokenDTO result = null;
		BpmError error = null;
		SubProcessCallResult callResult = SubProcessCall.withPath("OAuth2Feature").withStartName("getToken")
				.withParam("form", form).withParam("tokenUrlPrefix", tokenUrlPrefix)
				.withParam("tokenUrlSuffix", tokenUrlSuffix).call();

		if (callResult != null) {
			Optional<Object> o = Optional.ofNullable(callResult.get("token"));
			if (o.isPresent()) {
				result = (TokenDTO) o.get();
			} else {
				Optional<Object> e = Optional.ofNullable(callResult.get("error"));
				if (e.isPresent()) {
					error = (BpmError) e.get();
					LOG.error(error);
					throw error;
				}
			}
		}

		if (null == result || StringUtils.isBlank(result.getAccessToken())) {
			LOG.error("access token cannot be null or empty");
			throw MailStoreService.buildError("getToken").withMessage("access token cannot be null or empty").build();
		}

		return result.getAccessToken();
	}
	
}