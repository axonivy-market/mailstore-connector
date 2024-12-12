package com.axonivy.connector.oauth.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

public class MailStoreTrustManager implements X509TrustManager {
	final X509TrustManager defaultTrustManager;
	final X509TrustManager customTrustManager;

	public MailStoreTrustManager(X509TrustManager defaultTrustManager, X509TrustManager customTrustManager) {
		this.defaultTrustManager = defaultTrustManager;
		this.customTrustManager = customTrustManager;
	}

	private X509Certificate[] mergeCertificates() {
		ArrayList<X509Certificate> resultingCerts = new ArrayList<>();
		resultingCerts.addAll(Arrays.asList(defaultTrustManager.getAcceptedIssuers()));
		resultingCerts.addAll(Arrays.asList(customTrustManager.getAcceptedIssuers()));
		return resultingCerts.toArray(new X509Certificate[resultingCerts.size()]);
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return mergeCertificates();
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		try {
			customTrustManager.checkServerTrusted(chain, authType);
		} catch (CertificateException e) {
			defaultTrustManager.checkServerTrusted(chain, authType);
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		defaultTrustManager.checkClientTrusted(mergeCertificates(), authType);
	}
}
