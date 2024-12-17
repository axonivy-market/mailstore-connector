package com.axonivy.connector.oauth.ssl;

import static com.axonivy.connector.mailstore.constant.Constants.DEFAULT_CONFIGURATION_FOLDER;
import static com.axonivy.connector.mailstore.constant.Constants.DEFAULT_IVY_FILE;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import ch.ivyteam.config.ConfigFile;
import ch.ivyteam.ivy.config.IFileAccess;
import ch.ivyteam.ivy.environment.Ivy;

@SuppressWarnings("restriction")
public class TrustStoreFileReader extends ConfigFile {
	private static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();
	private static final String DEFAULT_FILE_NAME = "truststore.p12";
	private static final String SSL_TRUSTSTORE_FILE_KEY = "SSL.Client.TrustStore.File";
	private static final String SSL_TRUSTSTORE_PASS_KEY = "SSL.Client.TrustStore.Password";
	private Properties properties;

	public TrustStoreFileReader() {
		super(new File(DEFAULT_CONFIGURATION_FOLDER), DEFAULT_IVY_FILE);
		getProperties();
	}

	public File getTrustFile() {
		String filePath = readPropertyValue(SSL_TRUSTSTORE_FILE_KEY, DEFAULT_FILE_NAME);
		File store = new File(filePath);
		if (!store.isAbsolute()) {
			store = IFileAccess.instance().getConfigFile(DEFAULT_FILE_NAME).toFile();
		}
		return store;
	}

	public char[] getTrustPassword() {
		String pass = readPropertyValue(SSL_TRUSTSTORE_PASS_KEY);
		if (StringUtils.isAllBlank(pass)) {
			return DEFAULT_PASSWORD;
		}
		return pass.toCharArray();
	}

	private String readPropertyValue(String key, String... defaultValue) {
		var defaultProperty = defaultValue.length == 0 ? StringUtils.EMPTY : defaultValue[0];
		return Optional.ofNullable(properties).map(pro -> pro.getProperty(key)).orElse(defaultProperty);
	}

	private void getProperties() {
		try {
			properties = readAsProperties();
		} catch (IOException e) {
			Ivy.log().error("Failed to read ivy config as properties", e);
		}
	}
}
