package com.axonivy.market.mailstore.connector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.lang3.StringUtils;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.bpm.error.BpmPublicErrorBuilder;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.vars.Variable;
import ch.ivyteam.log.Logger;

public class MailStoreService {
	private static final MailStoreService INSTANCE = new MailStoreService();
	private static final Logger LOG = Ivy.log();
	private static final String MAIL_STORE_PROTOCOL_VAR = "mailstore-connector.protocol";
	private static final String MAIL_STORE_HOST_VAR = "mailstore-connector.host";
	private static final String MAIL_STORE_PORT_VAR = "mailstore-connector.port";
	private static final String MAIL_STORE_USER_VAR = "mailstore-connector.user";
	private static final String MAIL_STORE_PASSWORD_VAR = "mailstore-connector.password";
	private static final String MAIL_STORE_DEBUG_VAR = "mailstore-connector.debug";
	private static final String MAIL_STORE_PROPERTIES_VAR = "mailstore-connector.properties";
	private static final String ERROR_BASE = "mailstore:connector";

	public static MailStoreService get() {
		return INSTANCE;
	}

	public void test() throws NoSuchProviderException {
		LOG.info("Test call");

		LOG.info("Variables");
		for (Variable variable : Ivy.var().all()) {
			LOG.info("{0}: {1} {2}", variable.name(), variable.type(), variable.value());
		}

		try (Store store = openStore()) {

		} catch (Exception e) {
			LOG.error("Error while working with mail store.", e);
		};
	}

	private Store openStore() {
		Store store = null;

		String protocol = Ivy.var().get(MAIL_STORE_PROTOCOL_VAR);
		String host = Ivy.var().get(MAIL_STORE_HOST_VAR);
		String portString = Ivy.var().get(MAIL_STORE_PORT_VAR);
		String user = Ivy.var().get(MAIL_STORE_USER_VAR);
		String password = Ivy.var().get(MAIL_STORE_PASSWORD_VAR);
		String debugString = Ivy.var().get(MAIL_STORE_DEBUG_VAR);

		LOG.info("Creating mail store connection, protocol: {0} host: {1} port: {2} user: {3} password: {4} debug: {5}",
				protocol, host, portString, user, StringUtils.isNotBlank(password) ? "is set" : "is not set", debugString);

		Properties properties = getProperties();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		PrintStream debugStream = new PrintStream(stream);

		boolean debug = true;

		try {
			debug = Boolean.parseBoolean(debugString);
			int port = Integer.parseInt(portString);

			Session session = Session.getDefaultInstance(properties, null);

			if(debug) {
				session.setDebug(debug);
				session.setDebugOut(debugStream);
			}
			store = session.getStore(protocol);
			store.connect(host, port, user, password);
		} catch(Exception e) {
			try {
				store.close();
			} catch (MessagingException closeEx) {
				LOG.error("Closing store caused another exception. Anyway the store is closed.", closeEx);
			}
			throw buildError("noconnection").withCause(e).build();
		}
		finally {
			if(debug) {
				LOG.debug("Debug output:\n {0}", stream.toString());
			}
		}
		return store;
	}

	private Properties getProperties() {
		// Properties properties = System.getProperties();
		Properties properties = new Properties();

		String propertiesPrefix = MAIL_STORE_PROPERTIES_VAR + ".";
		for (Variable variable : Ivy.var().all()) {
			String name = variable.name();
			if(name.startsWith(propertiesPrefix)) {
				String propertyName = name.substring(propertiesPrefix.length());
				String value = variable.value(); 
				LOG.info("Setting additional property {0}: ''{1}''", propertyName, value);
				properties.setProperty(name, value);
			}
		}

		return properties;
	}

	private BpmPublicErrorBuilder buildError(String code) {
		return BpmError.create(ERROR_BASE + ":" + code);
	}
}
