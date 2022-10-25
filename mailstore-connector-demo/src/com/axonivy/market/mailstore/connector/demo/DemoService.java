package com.axonivy.market.mailstore.connector.demo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;

import com.axonivy.market.mailstore.connector.MailStoreService;
import com.axonivy.market.mailstore.connector.MailStoreService.MessageIterator;
import com.axonivy.market.mailstore.connector.MessageService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

public class DemoService {
	private static final Logger LOG = Ivy.log();

	public static boolean handleMessage(Message message) throws MessagingException, IOException {
		LOG.info("Working on message {0} received at {1} type {2}", message.getSubject(), message.getReceivedDate(), message.getContent().getClass());

		Predicate<Part> lookPredicate = MessageService.isImage("*").or(Predicate.not(MessageService.isMessage("*")));
		Predicate<Part> collectPredicate = MessageService.isImage("*");
		Collection<Part> parts = MessageService.allParts(message, lookPredicate, collectPredicate);
		for (Part part : parts) {
			LOG.info("Part: Filename: {0} Description: {1} ContentType: {2} Disposition: {3} Content Class: {4}",
					part.getFileName(), part.getDescription(), part.getContentType(), part.getDisposition(), part.getContent().getClass());
		}
		return true;
	}

	public static void test() throws MessagingException, IOException {
		MessageIterator iterator = MailStoreService.messageIterator("localhost-imap", "INBOX", null, false, MailStoreService.subjectMatches(".*test.*", false));

		while (iterator.hasNext()) {
			Message message = iterator.next();

			InputStream stream = MailStoreService.saveMessage(message);
			message = MailStoreService.loadMessage(stream);

			boolean handled = handleMessage(message);
			iterator.handledMessage(handled);
		}
	}

	public static void main(String[] args) throws MessagingException, IOException {
		MessageIterator iterator = MailStoreService.messageIterator("localhost-imap", "INBOX", null, false, MailStoreService.subjectMatches(".*", false));

		while (iterator.hasNext()) {
			Message message = iterator.next();
			showMessage(message);
			iterator.handledMessage(true);
		}
	}

	private static void showMessage(Message message) throws MessagingException, IOException {

		try(ClassLoaderContext ccl = new ClassLoaderContext(Session.class)) {
			LOG.info("Got message: {0}", message.getSubject());
			LOG.info("Content: {0}", message.getContent());
		}
	}

	private static class ClassLoaderContext implements AutoCloseable {

		private ClassLoader originalClassLoader;
		public ClassLoaderContext(Class<?> clazz) {
			originalClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(clazz.getClassLoader());
		}

		@Override
		public void close() {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
}
