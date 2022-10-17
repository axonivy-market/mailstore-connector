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
	private static final String MAIL_STORE_VAR = "mailstore-connector";
	private static final String PROTOCOL_VAR = "protocol";
	private static final String HOST_VAR = "host";
	private static final String PORT_VAR = "port";
	private static final String USER_VAR = "user";
	private static final String PASSWORD_VAR = "password";
	private static final String DEBUG_VAR = "debug";
	private static final String PROPERTIES_VAR = "properties";
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

		String storeName;
		storeName = "ethereal-imaps";
		// storeName = "localhost-imap";

		try (Store store = openStore(storeName)) {

		} catch (Exception e) {
			LOG.error("Error while working with mail store.", e);
		};
	}

	private Store openStore(String storeName) {
		Store store = null;

		String protocol = getVar(storeName, PROTOCOL_VAR);
		String host = getVar(storeName, HOST_VAR);
		String portString = getVar(storeName, PORT_VAR);
		String user = getVar(storeName, USER_VAR);
		String password = getVar(storeName, PASSWORD_VAR);
		String debugString = getVar(storeName, DEBUG_VAR);

		LOG.debug("Creating mail store connection, protocol: {0} host: {1} port: {2} user: {3} password: {4} debug: {5}",
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

	private String getVar(String store, String var) {
		return Ivy.var().get(String.format("%s.%s.%s", MAIL_STORE_VAR, store, var));
	}

	private Properties getProperties() {
		// Properties properties = System.getProperties();
		Properties properties = new Properties();

		String propertiesPrefix = PROPERTIES_VAR + ".";
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
