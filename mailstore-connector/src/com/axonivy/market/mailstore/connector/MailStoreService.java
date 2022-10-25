package com.axonivy.market.mailstore.connector;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.mail.FetchProfile;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
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

	/**
	 * Get a {@link MessageIterator}.
	 * 
	 * @param storeName name of Email Store (Imap Configuration)
	 * @param srcFolderName source folder name
	 * @param dstFolderName destination folder name (if <code>null</code> then handled mails will be deleted)
	 * @param delete delete mail from source folder?
	 * @param filter a filter predicate
	 * @return
	 * @throws MessagingException
	 */
	public static MessageIterator messageIterator(String storeName, String srcFolderName, String dstFolderName, boolean delete, Predicate<Message> filter) {
		return new MessageIterator(storeName, srcFolderName, dstFolderName, delete, filter);
	}

	/**
	 * Get a {@link Predicate} to match subjects against a regular expression.
	 * 
	 * Note, that the full subject must match. If you want a "contains"
	 * match, use something like:
	 * 
	 * <pre>
	 * subjectRegexFilter(".*my matching pattern.*", false);
	 * </pre>
	 * 
	 * @param pattern
	 * @param caseSensitive
	 * @return
	 */
	public static Predicate<Message> subjectRegex(String pattern, boolean caseSensitive) {
		Pattern subjectPattern = Pattern.compile(pattern, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
		return m -> {
			try {
				return subjectPattern.matcher(m.getSubject()).matches();
			} catch (MessagingException e) {
				throw buildError("predicate:subjectregex").build();
			}
		};
	}

	/**
	 * Get a {@link Predicate} to match "from" addresses against a regular expression.
	 * 
	 * Note, that the full address must match. If you want a "contains"
	 * match, use something like:
	 * 
	 * <pre>
	 * fromRegexFilter(".*my matching pattern.*");
	 * </pre>
	 * 
	 * @param pattern
	 * @return
	 */
	public static Predicate<Message> fromRegex(String pattern) {
		Pattern fromPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		return m -> {
			try {
				return fromPattern.matcher(m.getFrom().toString()).matches();
			} catch (MessagingException e) {
				throw buildError("predicate:fromregex").build();
			}
		};
	}

	/**
	 * Iterate through the E-Mails of a store.
	 * 
	 * Optionally remove or move messages to a destination folder when they were handled.
	 * 
	 * Note that the {@link Iterator} will only close and return it's resources when it was
	 * running to the end. If it is terminated earlier, the {@link #close()} method must be
	 * called. It is not a problem, to call the close method on a closed object again.
	 */
	public static class MessageIterator implements Iterator<Message>, AutoCloseable {
		private Store store;
		private Folder srcFolder;
		private Folder dstFolder;
		private boolean delete;
		private Message[] messages;
		private int nextIndex;
		private ClassLoader originalClassLoader;

		private MessageIterator(String storeName, String srcFolderName, String dstFolderName, boolean delete, Predicate<Message> filter) {
			try {
				// Use own classloader so that internal classes of javax.mail API are found.
				// If they cannot be found on the classpath, then mail content will not
				// be recognized and always be reported as IMAPInputStream
				originalClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(Session.class.getClassLoader());

				this.delete = delete;
				store = MailStoreService.get().openStore(storeName);
				srcFolder = MailStoreService.get().openFolder(store, srcFolderName, Folder.READ_WRITE);
				if(StringUtils.isNotBlank(dstFolderName)) {
					dstFolder = MailStoreService.get().openFolder(store, dstFolderName, Folder.READ_WRITE);
				}
				messages = srcFolder.getMessages();

				// pre-fetch headers
				FetchProfile fetchProfile = new FetchProfile();
				fetchProfile.add(FetchProfile.Item.ENVELOPE);
				srcFolder.fetch(messages, fetchProfile);

				if(filter != null) {
					messages = Stream.of(messages).filter(filter).toArray(Message[]::new);
				}

				LOG.debug("Received {0}{1} messages.", messages.length, filter != null ? " matching" : "");

				nextIndex = 0;
			} catch(Exception e) {
				close();
				throw buildError("iterator").withCause(e).build();
			}
		}

		/**
		 * Close and sync all actions to the mail server.
		 * 
		 * Will be called automatically after the last element is fetched.
		 * Must be called if the iterator does not run to the end.
		 */
		@Override
		public void close() {
			Exception exception = null;
			if(dstFolder != null && dstFolder.isOpen()) {
				try {
					dstFolder.close();
				} catch (Exception e) {
					LOG.error("Could not close destination folder {0}", e, dstFolder);
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(srcFolder != null && srcFolder.isOpen()) {
				try {
					srcFolder.close();
				} catch (Exception e) {
					LOG.error("Could not close source folder {0}", e, srcFolder);
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(store != null) {
				try {
					store.close();
				} catch (Exception e) {
					LOG.error("Could not close store {0}", e, srcFolder);
					if(exception == null) {
						exception = e;
					}
				}
			}
			if(exception != null) {
				buildError("close").withCause(exception);
			}
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}

		@Override
		public boolean hasNext() {
			boolean hasNext = messages.length > nextIndex;
			if(!hasNext) {
				close();
			}
			return hasNext;
		}

		@Override
		public Message next() {
			try {
				Message current = messages[nextIndex];
				nextIndex += 1;
				return current;
			} catch (Exception e) {
				throw new NoSuchElementException("Could not access message at index: " + nextIndex + " length: " + (messages != null ? messages.length : "null"), e);
			}
		}

		/**
		 * Call this function, when the message was handled successfully and should be deleted/moved.
		 * 
		 * It will then be moved to the destination folder (if there is one)
		 * and will be deleted in the source folder (if the delete option is set).
		 * If this function is not called, the message will be coming again in the
		 * next iterator.
		 */
		public void handledMessage() {
			try {
				Message current = messages[nextIndex-1];
				if(dstFolder != null) {
					LOG.debug("Appending {0} to destination folder", MailStoreService.toString(current));
					dstFolder.appendMessages(new Message[] {current});
				}
				if(delete) {
					LOG.debug("Deleting {0}", MailStoreService.toString(current));
					current.setFlag(Flag.DELETED, true);
				}

				if(!hasNext()) {
					close();
				}
			} catch (Exception e) {
				throw buildError("handled").withCause(e).build();
			}
		}
	}

	/**
	 * Get a mail store.
	 * 
	 * Note, that it is recommended to use {@link MessageIterator}.
	 * 
	 * @param storeName
	 * @return
	 * @throws MessagingException
	 */
	public Store openStore(String storeName) throws MessagingException {
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
		} catch(MessagingException e) {
			try {
				store.close();
			} catch (MessagingException closeEx) {
				LOG.error("Closing store caused another exception. Anyway the store is closed.", closeEx);
			}
			throw (e);
		}
		finally {
			if(debug) {
				LOG.debug("Debug output:\n {0}", stream.toString());
			}
		}
		return store;
	}

	private Folder openFolder(Store store, String folderName, int mode) throws MessagingException {
		LOG.debug("Opening folder {0}", folderName);
		Folder folder = store.getFolder(folderName);

		if(folder != null && folder.exists()) {
			LOG.debug("Message count: {0} new: {1} unread: {2} deleted: {3}",
					folder.getMessageCount(), folder.getNewMessageCount(),
					folder.getUnreadMessageCount(), folder.getDeletedMessageCount());

			folder.open(mode);
		}
		else {
			throw new MessagingException("Could not open folder " + folderName);
		}

		return folder;
	}

	private String getVar(String store, String var) {
		return Ivy.var().get(String.format("%s.%s.%s", MAIL_STORE_VAR, store, var));
	}

	private Properties getProperties() {
		Properties properties = System.getProperties();

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

	private static BpmPublicErrorBuilder buildError(String code) {
		BpmPublicErrorBuilder builder = BpmError.create(ERROR_BASE + ":" + code);
		return builder;
	}

	private static String toString(Message m) {
		String subject = null;
		try {
			subject = m != null ? m.getSubject() : null;
		} catch (MessagingException e) {
			subject = "Exception while reading subject";
		}
		return String.format("Message[subject: '%s']", subject);
	}
}
