package com.axonivy.connector.mailstore.enums;

public enum StartTLS {
	ENABLE("mail.imap.starttls.enable"), REQUIRED("mail.imap.starttls.required");

	private String property;

	private StartTLS(String property) {
		this.property = property;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

}
