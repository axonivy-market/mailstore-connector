package com.axonivy.connector.oauth.ssl;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

import ch.ivyteam.ivy.environment.Ivy;

public class MailStoreTrustManager implements X509TrustManager {
	final X509TrustManager defaultTrustManager;
	final X509TrustManager customTrustManager;

	public MailStoreTrustManager(X509TrustManager defaultTrustManager, X509TrustManager customTrustManager) {
		this.defaultTrustManager = defaultTrustManager;
		this.customTrustManager = customTrustManager;
	}

	private X509Certificate[] mergeCertificates() {
		ArrayList<X509Certificate> resultingCerts = new ArrayList<>();
		if (defaultTrustManager != null) {
			resultingCerts.addAll(Arrays.asList(defaultTrustManager.getAcceptedIssuers()));
		}
		if (customTrustManager != null) {
			resultingCerts.addAll(Arrays.asList(customTrustManager.getAcceptedIssuers()));
		}
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
		} catch (Exception e) {
			Ivy.log().error("Cannot read TrustedServer from TrustManager", e);
			if (defaultTrustManager != null) {
				defaultTrustManager.checkServerTrusted(chain, authType);
			}
		}
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (defaultTrustManager == null) {
			return;
		}
		defaultTrustManager.checkClientTrusted(mergeCertificates(), authType);
	}
}
