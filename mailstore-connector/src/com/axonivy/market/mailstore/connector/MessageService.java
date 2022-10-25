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

	/**
	 * Get a flat list of all parts according to the given predicates.
	 * 
	 * Look at Java {@link Predicate}s to find examples of powerful
	 * combinations of predicates.
	 * 
	 * @param message
	 * @param lookPredicate only look (and go into) at parts which match the predicate
	 * @param collectPredicate only return parts matching the predicate
	 * @return
	 */
	public static Collection<Part> allParts(Message message, Predicate<Part> lookPredicate, Predicate<Part> collectPredicate) {
		List<Part> parts = new ArrayList<>();
		try {
			collectParts(parts, message, 1, lookPredicate, collectPredicate);
		}
		catch(Exception e) {
			throw buildError("iterator").withCause(e).build();
		}

		return parts;
	}

	private static void collectParts(List<Part> parts, Part part, int level, Predicate<Part> lookPredicate, Predicate<Part> collectPredicate) throws MessagingException, IOException {
		if(lookPredicate == null || lookPredicate.test(part)) {
			if(collectPredicate == null || collectPredicate.test(part)) {
				parts.add(part);
			}
			if(isMultipart("*").test(part)) {
				MimeMultipart multipart = (MimeMultipart) part.getContent();
				int count = multipart.getCount();
				for (int i = 0; i < count; i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					collectParts(parts, bodyPart, level+1, lookPredicate, collectPredicate);
				}
			}
			else if(isMessage("*").test(part)) {
				Message message = (Message) part.getContent();
				collectParts(parts, message, level+1, lookPredicate, collectPredicate);
			}
		}
	}

	/**
	 * Does part have the given MIME type?
	 * 
	 * For the sub-type an asterisk (*) acts as a wildcard.
	 * 
	 * @param mimeType
	 * @return
	 */
	public static Predicate<Part> isMimeType(String mimeType) {
		return p -> {
			try {
				return p.isMimeType(mimeType);
			} catch (MessagingException e) {
				throw buildError("predicate:mimetype").build();
			}
		};
	}

	/**
	 * Does part have the given MIME type?
	 * 
	 * @param mimeType
	 * @param subType use asterisk (*) as wildcard.
	 * @return
	 */
	public static Predicate<Part> isMimeType(String mimeType, String subType) {
		return isMimeType(mimeType + "/" + (subType != null ? subType : "*"));
	}

	/**
	 * Is this an image part?
	 * 
	 * @param subType use asterisk (*) as wildcard.
	 * @return
	 */
	public static Predicate<Part> isImage(String subType) {
		return isMimeType("image", subType);
	}

	/**
	 * Is this an image part?
	 * 
	 * @param subType use asterisk (*) as wildcard.
	 * @return
	 */
	public static Predicate<Part> isText(String subType) {
		return isMimeType("text", subType);
	}

	/**
	 * Is this a message part?
	 * 
	 * @param subType use asterisk (*) as wildcard.
	 * @return
	 */
	public static Predicate<Part> isMessage(String subType) {
		return isMimeType("message", subType);
	}

	/**
	 * Is this a multipart?
	 * 
	 * @param subType use asterisk (*) as wildcard.
	 * @return
	 */
	public static Predicate<Part> isMultipart(String subType) {
		return isMimeType("multipart", subType);
	}

	/**
	 * Is this an attachment?
	 * 
	 * @return
	 */
	public static Predicate<Part> isAttachment() {
		return p -> {
			try {
				return Message.ATTACHMENT.equals(p.getDisposition());
			} catch (MessagingException e) {
				throw buildError("predicate:attachment").build();
			}
		}; 
	}

	/**
	 * Is this an attachment?
	 * 
	 * @return
	 */
	public static Predicate<Part> isInline() {
		return p -> {
			try {
				return Message.INLINE.equals(p.getDisposition());
			} catch (MessagingException e) {
				throw buildError("predicate:inline").build();
			}
		}; 
	}

	/**
	 * Is this the parent or more parts?
	 * 
	 * @return
	 */
	public static Predicate<Part> isParent() {
		return isMultipart("*").or(isMessage("*"));
	}

	/**
	 * Is this the parent or more parts?
	 * 
	 * @return
	 */
	public static Predicate<Part> isParent(boolean includeSubMessages) {
		return includeSubMessages ? isParent() : Predicate.not(isMessage("*"));
	}

	private static BpmPublicErrorBuilder buildError(String code) {
		BpmPublicErrorBuilder builder = BpmError.create(ERROR_BASE + ":" + code);
		return builder;
	}
}
