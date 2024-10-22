package com.axonivy.connector.oauth;

public class FormDTO {
	private String tenantId;
	private String clientId;
	private String clientSecret;
	private String scope;
	private String grantType;
	
	public FormDTO() {
	}
	
	public FormDTO(String tenantId, String clientId, String clientSecret, String scope, String grantType) {
		super();
		this.tenantId = tenantId;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.scope = scope;
		this.grantType = grantType;
	}
	
	public String getTenantId() {
		return tenantId;
	}
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public String getScope() {
		return scope;
	}
	public void setScope(String scope) {
		this.scope = scope;
	}
	public String getGrantType() {
		return grantType;
	}
	public void setGrantType(String grantType) {
		this.grantType = grantType;
	}
	
}
