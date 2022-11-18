package com.axonivy.connector.mailstore.demo;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Predicate;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;

import com.axonivy.connector.mailstore.MailStoreService;
import com.axonivy.connector.mailstore.MessageService;
import com.axonivy.connector.mailstore.MailStoreService.MessageIterator;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

public class DemoService {
	private static final Logger LOG = Ivy.log();

	public static void handleMessages() throws MessagingException, IOException {
		MessageIterator iterator = MailStoreService.messageIterator("localhost-imap", "INBOX", null, false, MailStoreService.subjectMatches(".*test [0-9]+.*"));

		while (iterator.hasNext()) {
			Message message = iterator.next();

			boolean handled = handleMessage(message);
			iterator.handledMessage(handled);
		}
	}

	public static boolean handleMessage(Message message) throws MessagingException, IOException {
		LOG.info("Working on message {0} received at {1} type {2}", message.getSubject(), message.getReceivedDate(), message.getContent().getClass());

		Predicate<Part> collectPredicate = MessageService.isImage("*");
		Collection<Part> parts = MessageService.getAllParts(message, false, collectPredicate);
		for (Part part : parts) {
			LOG.info("Part: Filename: {0} Description: {1} ContentType: {2} Disposition: {3} Content Class: {4}",
					part.getFileName(), part.getDescription(), part.getContentType(), part.getDisposition(), part.getContent().getClass());
		}
		return true;
	}

}
