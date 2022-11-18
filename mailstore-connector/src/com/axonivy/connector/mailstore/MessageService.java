package com.axonivy.connector.mailstore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.MimeMultipart;

import ch.ivyteam.ivy.bpm.error.BpmError;
import ch.ivyteam.ivy.bpm.error.BpmPublicErrorBuilder;

public class MessageService {
	private static final MessageService INSTANCE = new MessageService();
	// private static final Logger LOG = Ivy.log();
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
	 * @param filter only return parts matching the predicate
	 * @return
	 */
	public static List<Part> getAllParts(Message message, boolean includeSubMessages, Predicate<Part> filter) {
		List<Part> parts = new ArrayList<>();
		try {
			collectParts(parts, message, 1, includeSubMessages, filter);
		}
		catch(Exception e) {
			throw buildError("iterator").withCause(e).build();
		}

		return parts;
	}

	/**
	 * Get all text parts concatenated into a single {@link String}.
	 * 
	 * @param message
	 * @param subType
	 * @param delimiter
	 * @param includeSubMessages
	 * @return
	 */
	public static String getAllTexts(Message message, String subType, String delimiter, boolean includeSubMessages) {
		return getAllParts(message, includeSubMessages, isText(subType))
				.stream()
				.map(p -> {
					try {
						String content = (String)p.getContent();
						return content != null ? content : "<null>";
					} catch (IOException | MessagingException e) {
						throw buildError("alltexts").build();
					}
				})
				.collect(Collectors.joining(delimiter));
	}

	/**
	 * Get all plain texts concatenated into a single {@link String}.
	 * 
	 * @param message
	 * @param delimiter
	 * @param includeSubMessages
	 * @return
	 */
	public static String getAllPlainTexts(Message message, String delimiter, boolean includeSubMessages) {
		return getAllTexts(message, "plain", delimiter, includeSubMessages);
	}

	/**
	 * Get all HTML texts concatenated into a single {@link String}.
	 * 
	 * @param message
	 * @param delimiter
	 * @param includeSubMessages
	 * @return
	 */
	public static String getAllHtmls(Message message, String delimiter, boolean includeSubMessages) {
		return getAllTexts(message, "html", delimiter, includeSubMessages);
	}

	/**
	 * Get the binary content of this part as a Stream.
	 * 
	 * @param part
	 * @return
	 */
	public static InputStream getBinaryContentStream(Part part) {
		try {
			return (InputStream)part.getContent();
		} catch (IOException | MessagingException e) {
			throw buildError("binarycontent").build();
		}
	}

	/**
	 * Get the binary content bytes of this part.
	 * 
	 * @param part
	 * @return
	 */
	public static byte[] getBinaryContent(Part part) {
		try {
			return getBinaryContentStream(part).readAllBytes();
		} catch (IOException e) {
			throw buildError("binarycontent").build();
		}
	}

	private static void collectParts(List<Part> parts, Part part, int level, boolean includeSubMessages, Predicate<Part> filter) throws MessagingException, IOException {
		if(includeSubMessages || level == 1 || !(part instanceof Message)) {
			if(filter == null || filter.test(part)) {
				parts.add(part);
			}
			if(isMultipart("*").test(part)) {
				MimeMultipart multipart = (MimeMultipart) part.getContent();
				int count = multipart.getCount();
				for (int i = 0; i < count; i++) {
					BodyPart bodyPart = multipart.getBodyPart(i);
					collectParts(parts, bodyPart, level+1, includeSubMessages, filter);
				}
			}
			else if(isMessage("*").test(part)) {
				Message message = (Message) part.getContent();
				collectParts(parts, message, level+1, includeSubMessages, filter);
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
	 * Is this the correct disposition type?
	 * 
	 * @return
	 */
	public static Predicate<Part> isDisposition(String disposition) {
		return p -> {
			try {
				return disposition.equals(p.getDisposition());
			} catch (MessagingException e) {
				throw buildError("predicate:disposition").build();
			}
		}; 
	}

	/**
	 * Is this an attachment?
	 * 
	 * @return
	 */
	public static Predicate<Part> isAttachment() {
		return isDisposition(Message.ATTACHMENT); 
	}

	/**
	 * Is this an attachment?
	 * 
	 * @return
	 */
	public static Predicate<Part> isInline() {
		return isDisposition(Message.INLINE);
	}

	public static Predicate<Part> filenameMatches(String pattern) {
		Pattern namePattern = createStandardPattern(pattern);
		return p -> {
			try {
				String fileName = p.getFileName();
				return fileName != null ? namePattern.matcher(fileName).matches() : false;
			} catch (MessagingException e) {
				throw buildError("predicate:filename").build();
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

	/**
	 * Always return true.
	 * 
	 * @return
	 */
	public static Predicate<Part> alwaysTrue() {
		return m -> true;
	}

	/**
	 * Always return false.
	 * 
	 * @return
	 */
	public static Predicate<Part> alwaysFalse() {
		return m -> false;
	}

	private static Pattern createStandardPattern(String pattern) {
		return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	}

	private static BpmPublicErrorBuilder buildError(String code) {
		BpmPublicErrorBuilder builder = BpmError.create(ERROR_BASE + ":" + code);
		return builder;
	}
}
