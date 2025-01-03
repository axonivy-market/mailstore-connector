package com.axonivy.connector.oauth.ssl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Properties;
import java.util.stream.Stream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang3.BooleanUtils;

import com.axonivy.connector.mailstore.constant.Constants;
import com.axonivy.connector.mailstore.enums.StartTLS;

import ch.ivyteam.ivy.ssl.client.restricted.SslClientSettings;

public class SSLContextConfigure {
	private static final SSLContextConfigure INSTANCE = new SSLContextConfigure();
	private SSLContextConfigure() {}
	
	public static SSLContextConfigure get() {
		return INSTANCE;
	}

	public boolean isStartTLSEnabled(Properties properties) {
		if (properties == null) {
			return false;
		}
		return BooleanUtils.toBoolean(properties.getProperty(StartTLS.ENABLE.getProperty()))
				|| BooleanUtils.toBoolean(properties.getProperty(StartTLS.REQUIRED.getProperty()));
	}

	@SuppressWarnings("restriction")
	public void addIvyTrustStoreToCurrentContext() throws NoSuchAlgorithmException, KeyStoreException,
			CertificateException, IOException, KeyManagementException {
		TrustManagerFactory tmFactory = initDefaultTrustManagerFactory();
		// Backup default Certificates
		X509TrustManager defaultX509CertTM = getFirstX509TrustManagerFromFactory(tmFactory);

		var currentSSLClientSettings = SslClientSettings.instance();
		try (InputStream trustStoreStream = new FileInputStream(currentSSLClientSettings.getTrustStoreFile())) {
			KeyStore ivyTrustStore = KeyStore.getInstance(currentSSLClientSettings.getTrustStoreType());
			ivyTrustStore.load(trustStoreStream, currentSSLClientSettings.getTrustStorePassword());
			tmFactory = getDefaultAlgorithm();
			tmFactory.init(ivyTrustStore);
			X509TrustManager ivyTrustManager = getFirstX509TrustManagerFromFactory(tmFactory);
			// Merge ivyTrustStrore with defaultTrustStore then build SSLContext
			buildSSLContext(new MailStoreTrustManager(defaultX509CertTM, ivyTrustManager));
		}
	}

	private TrustManagerFactory getDefaultAlgorithm() throws NoSuchAlgorithmException {
		return TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	}

	private X509TrustManager getFirstX509TrustManagerFromFactory(TrustManagerFactory trustManagerFactory) {
		return Stream.of(trustManagerFactory.getTrustManagers())
				.filter(trustManager -> trustManager instanceof X509TrustManager)
				.findFirst().map(trustManager -> (X509TrustManager) trustManager)
				.orElse(null);
	}

	private TrustManagerFactory initDefaultTrustManagerFactory() throws NoSuchAlgorithmException, KeyStoreException {
		TrustManagerFactory trustManagerFactory = getDefaultAlgorithm();
		trustManagerFactory.init((KeyStore) null);
		return trustManagerFactory;
	}

	public void resetToDefaultTrustStore() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
		TrustManagerFactory trustManagerFactory = initDefaultTrustManagerFactory();
		X509TrustManager defaultX509CertTM = getFirstX509TrustManagerFromFactory(trustManagerFactory);
		buildSSLContext(defaultX509CertTM);
	}

	private void buildSSLContext(X509TrustManager x509CertTrustManager)
			throws NoSuchAlgorithmException, KeyManagementException {
		SSLContext sslContext = SSLContext.getInstance(Constants.TLS);
		sslContext.init(null, new TrustManager[] { x509CertTrustManager }, null);
		SSLContext.setDefault(sslContext);
	}
}
