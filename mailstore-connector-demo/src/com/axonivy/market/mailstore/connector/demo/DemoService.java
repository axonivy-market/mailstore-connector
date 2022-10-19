package com.axonivy.market.mailstore.connector.demo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;

import com.axonivy.market.mailstore.connector.MailStoreService;
import com.axonivy.market.mailstore.connector.MailStoreService.MessageIterator;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

public class DemoService {
	private static final Logger LOG = Ivy.log();

	public static void handleMessage(Message message) throws MessagingException, IOException {
		LOG.info("Working on message {0} received at {1}", message.getSubject(), message.getReceivedDate());
		LOG.info("Other: {0}", message.getContent());

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		message.writeTo(bos);

		LOG.info("Content:\n{0}", bos.toString());

	}

	public static void main(String[] args) throws MessagingException, IOException {
		MessageIterator iterator = MailStoreService.mailIterator("localhost-imap", "INBOX", null, false, null);

		while (iterator.hasNext()) {
			Message message = iterator.next();
			LOG.info("Got message: {0}", message.getSubject());
			LOG.info("Content: {0}", message.getContent());
			iterator.handledMessage();
		}
	}
}
