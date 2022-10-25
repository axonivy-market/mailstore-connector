package com.axonivy.market.mailstore.connector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.bpm.error.BpmPublicErrorBuilder;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.log.Logger;

public class MessageService {
	private static final MessageService INSTANCE = new MessageService();
	private static final Logger LOG = Ivy.log();
	private static final String ERROR_BASE = "mailstore:connector:message";

	public static MessageService get() {
		return INSTANCE;
	}

	public static Collection<Part> allParts(Message message, Predicate<Part> predicate) {
		List<Part> parts = new ArrayList<>();
		try {
			collectParts(parts, message, 1, predicate);
		}
		catch(Exception e) {
			throw buildError("iterator").withCause(e).build();
		}

		return parts;
	}

	private static void collectParts(List<Part> parts, Part part, int level, Predicate<Part> predicate) throws MessagingException, IOException {
		if(predicate == null || predicate.test(part)) {
			parts.add(part);
			if(part.isMimeType("multipart/*")) {
				MimeMultipart multipart = (MimeMultipart) part.getContent();
				int count = multipart.getCount();
				for (int i = 0; i < count; i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					collectParts(parts, bodyPart, level+1, predicate);
				}
			}
			else if(part.isMimeType("message/*")) {
				Message message = (Message) part.getContent();
				collectParts(parts, message, level+1, predicate);
			}
		}
	}

	public static Predicate<Part> mimeType(String mimeType) {
		return p -> {
			try {
				return p.isMimeType(mimeType);
			} catch (MessagingException e) {
				throw buildError("predicate:mimetype").build();
			}
		};
	}

	public static Predicate<Part> noSubMessage() {
		return Predicate.not(mimeType("message/*"));
	}



	private static BpmPublicErrorBuilder buildError(String code) {
		BpmPublicErrorBuilder builder = BpmError.create(ERROR_BASE + ":" + code);
		return builder;
	}
}
