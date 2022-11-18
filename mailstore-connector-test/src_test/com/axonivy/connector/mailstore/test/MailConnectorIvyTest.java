package com.axonivy.connector.mailstore.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.mail.Message;
import javax.mail.Part;

import org.junit.jupiter.api.Test;

import com.axonivy.connector.mailstore.MailStoreService;
import com.axonivy.connector.mailstore.MessageService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.environment.IvyTest;

/**
 * This sample UnitTest runs java code in an environment as it would exists when
 * being executed in Ivy Process. Popular projects API facades, such as {@link Ivy#persistence()}
 * are setup and ready to be used.
 * 
 * <p>The test can either be run<ul>
 * <li>in the Designer IDE ( <code>right click > run as > JUnit Test </code> )</li>
 * <li>or in a Maven continuous integration build pipeline ( <code>mvn clean verify</code> )</li>
 * </ul></p>
 * 
 * <p>Detailed guidance on writing these kind of tests can be found in our
 * <a href="https://developer.axonivy.com/doc/10.0/concepts/testing/unit-testing.html">Unit Testing docs</a>
 * </p>
 */
@IvyTest
public class MailConnectorIvyTest{

	/**
	 * Basic Email (not MIME).
	 */
	@Test
	public void testBasicMail() {
		Message message = readMessage("testmails/mail-basic.eml");

		Collection<Part> parts;

		parts = MessageService.getAllParts(message, true, null);
		assertThat(parts).hasSize(1);
	}

	/**
	 * Simple Email with MIME header.
	 */
	@Test
	public void testSimpleMail() {
		Message message = readMessage("testmails/mail-simple.eml");

		Collection<Part> parts;

		parts = MessageService.getAllParts(message, true, null);
		assertThat(parts).hasSize(1);
	}

	/**
	 * Test Email with attachment.
	 * 
	 * The Email structure is:
	 * 
	 * <pre>
	 * multipart/mixed (message)
	 *   text/plain
	 *   image/jpeg pst.jpg attachment
	 * </pre>
	 */
	@Test
	public void testMailWithAttachment(){
		Message message = readMessage("testmails/mail-with-attachment.eml");

		Collection<Part> parts;

		parts = MessageService.getAllParts(message, true, null);
		assertThat(parts).hasSize(3);

		parts = MessageService.getAllParts(message, true, MessageService.isImage("*"));
		assertThat(parts).hasSize(1);

		parts = MessageService.getAllParts(message, true, MessageService.isAttachment());
		assertThat(parts).hasSize(1);

		parts = MessageService.getAllParts(message, true, MessageService.isInline());
		assertThat(parts).isEmpty();
	}

	/**
	 * Test complex Email with inline and attachments.
	 * 
	 * The Email structure is:
	 * 
	 * <pre>
	 * multipart/mixed (message)
	 *   multipart/alternative
	 *     text/plain
	 *     multipart/related
	 *     text/html
	 *   image/jpeg pst.jpg inline
	 *   image/jpeg pst.jpg attachment
	 *   image/png bla.png attachment
	 * </pre>
	 */
	@Test
	public void testMailWithInlinesAndAttachment() {
		Message message = readMessage("testmails/mail-with-inline-and-attachments.eml");

		Collection<Part> parts;

		parts = MessageService.getAllParts(message, true, null);
		assertThat(parts).hasSize(8);

		parts = MessageService.getAllParts(message, true, MessageService.isImage("*"));
		assertThat(parts).hasSize(3);

		parts = MessageService.getAllParts(message, true, MessageService.isAttachment());
		assertThat(parts).hasSize(2);

		parts = MessageService.getAllParts(message, true, MessageService.isAttachment().and(MessageService.isImage("jpeg")));
		assertThat(parts).hasSize(1);

		parts = MessageService.getAllParts(message, true, MessageService.isInline());
		assertThat(parts).hasSize(1);
	}

	/**
	 * Test complex Email with attached Email.
	 * 
	 * The Email structure is:
	 * 
	 * <pre>
	 * multipart/mixed (message)
	 *   multipart/alternative
	 *     text/plain
	 *     multipart/related
	 *     text/html
	 *   image/png hWZTSwAK0sVfqRbX.png inline
	 *   message/rfc822 attachment
	 *     multipart/mixed
	 *       text/plain
	 *       image/jpeg bla.jpg attachment
	 *   image/jpeg pst.jpg attachment
	 * </pre>
	 */
	@Test
	public void testMailWithAttachedEmailAndInlinesAndAttachment() {
		Message message = readMessage("testmails/mail-with-attached-email-and-inline-and-attachments.eml");

		Collection<Part> parts;

		parts = MessageService.getAllParts(message, true, null);
		assertThat(parts).hasSize(11);

		// includes the attachment in the attached email
		parts = MessageService.getAllParts(message, true, MessageService.isAttachment());
		assertThat(parts).hasSize(3);

		// does not include the attachment in the attached email
		parts = MessageService.getAllParts(message, false, MessageService.isAttachment());
		assertThat(parts).hasSize(2);
	}

	/**
	 * Test filtering by address.
	 */
	@Test
	public void testFilteringByAddress() {
		List<Message> messages = allTestMessages();

		assertThat(messages.stream().filter(MailStoreService.toMatches(".*debug@localdomain.test.*")).count()).isEqualTo(5);
		assertThat(messages.stream().filter(MailStoreService.toMatches(".*second@somewhere.test.*")).count()).isEqualTo(1);
		assertThat(messages.stream().filter(MailStoreService.ccMatches(".*cc@somewhere.test.*")).count()).isEqualTo(1);

		assertThat(messages.stream().filter(MailStoreService.anyRecipientMatches(".*cc@somewhere.test.*")).count()).isEqualTo(1);
	}

	/**
	 * Test filtering by address.
	 */
	@Test
	public void testFilteringByHeader() {
		List<Message> messages = allTestMessages();
		assertThat(messages.stream().filter(MailStoreService.headerMatches("Message-ID", ".*97298fba-d3e4-1a6b-2116-8919d2ef8073.*")).count()).isEqualTo(1);
		assertThat(messages.stream().filter(MailStoreService.headerMatches("Reply-To", ".*debug@localdomain.test.*")).count()).isEqualTo(1);
		assertThat(messages.stream().filter(MailStoreService.headerMatches("Reply-To", ".*second-reply@somewhere.test.*")).count()).isEqualTo(1);
		assertThat(messages.stream().filter(MailStoreService.headerMatches("Received", ".*eBBAHoE9WmMFAgAAKN6DOg.*")).count()).isEqualTo(1);
		assertThat(messages.stream().filter(MailStoreService.headerMatches("Received", ".*67E4262CBD.*")).count()).isEqualTo(1);
	}

	/**
	 * Test filtering by part type.
	 */
	@Test
	public void testFilteringByPartType() {
		List<Message> messages = allTestMessages();

		assertThat(messages.stream().filter(MailStoreService.hasAttachment(false)).count()).isEqualTo(3);
		assertThat(messages.stream().filter(MailStoreService.hasPart("message/rfc822", Message.ATTACHMENT, ".*with attachment.*", false)).count()).isEqualTo(1);
		assertThat(messages.stream().filter(MailStoreService.hasPart("message/*", null, null, false)).count()).isEqualTo(1);
		assertThat(messages.stream().filter(MailStoreService.hasPart(null, null, "pst\\.jpg", false)).count()).isEqualTo(3);
	}

	@Test
	public void testMailContent() {
		Message message = readMessage("testmails/mail-with-inline-and-attachments.eml");

		assertThat(MessageService.getAllPlainTexts(message, ",", false)).contains("Test Mail");

		List<Part> parts = MessageService.getAllParts(message, false, MessageService.isImage("*").and(MessageService.isAttachment()));

		assertThat(parts).hasSize(2);

		assertThat(MessageService.getBinaryContent(parts.get(0))).hasSize(6964);
		assertThat(MessageService.getBinaryContent(parts.get(1))).hasSize(4743);
	}

	private List<Message> allTestMessages() {
		return List.of(
				readMessage("testmails/mail-basic.eml"),
				readMessage("testmails/mail-simple.eml"),
				readMessage("testmails/mail-with-multiple-recipients.eml"),
				readMessage("testmails/mail-with-attachment.eml"),
				readMessage("testmails/mail-with-inline-and-attachments.eml"),
				readMessage("testmails/mail-with-attached-email-and-inline-and-attachments.eml")
				);
	}

	private Message readMessage(String path) {
		InputStream stream = MailConnectorIvyTest.class.getResourceAsStream(path);
		assertThat(stream).isNotNull();

		Message message = MailStoreService.loadMessage(stream);
		return message;
	}
}
