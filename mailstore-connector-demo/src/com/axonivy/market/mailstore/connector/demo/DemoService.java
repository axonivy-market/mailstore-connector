package com.axonivy.market.mailstore.connector.demo;

import java.io.IOException;
import java.util.Collection;

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
		LOG.info("Working on message {0} received at {1}", message.getSubject(), message.getReceivedDate());
		LOG.info("Class: {0}", message.getContent().getClass());

		Collection<Part> parts = MessageService.allParts(message,null);
		for (Part part : parts) {
			LOG.info("Part: {0} {1} {2} {3}", part.getFileName(), part.getContentType(), part.getDisposition(), part.getContent().getClass());
		}

		//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		//		message.writeTo(bos);

		//		LOG.info("Content:\n{0}", bos.toString());
		return true;
	}

	public static void test() throws MessagingException, IOException {
		MessageIterator iterator = MailStoreService.messageIterator("localhost-imap", "INBOX", null, false, MailStoreService.subjectRegex(".*test 1[03].*", false));

		while (iterator.hasNext()) {
			Message message = iterator.next();
			boolean handled = handleMessage(message);
			iterator.handledMessage(handled);
		}
	}

	public static void main(String[] args) throws MessagingException, IOException {
		MessageIterator iterator = MailStoreService.messageIterator("localhost-imap", "INBOX", null, false, MailStoreService.subjectRegex(".*test 1[03].*", false));

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
