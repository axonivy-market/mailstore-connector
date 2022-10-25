package com.axonivy.market.mailstore.connector.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.Collection;

import javax.mail.Message;
import javax.mail.Part;

import org.junit.jupiter.api.Test;

import com.axonivy.market.mailstore.connector.MailStoreService;
import com.axonivy.market.mailstore.connector.MessageService;

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

	@Test
	public void testMailWithAttachment(){
		InputStream stream = MailConnectorIvyTest.class.getResourceAsStream("testmails/mail-with-attachment.eml");
		assertThat(stream).isNotNull();

		Message message = MailStoreService.loadMessage(stream);
		Collection<Part> parts;

		parts = MessageService.allParts(message, null, null);
		assertThat(parts).hasSize(3);

		parts = MessageService.allParts(message, null, MessageService.isImage("*"));
		assertThat(parts).hasSize(1);

		parts = MessageService.allParts(message, null, MessageService.isAttachment());
		assertThat(parts).hasSize(1);

		parts = MessageService.allParts(message, null, MessageService.isInline());
		assertThat(parts).isEmpty();
	}

	@Test
	public void testMailWithInlinesAndAttachment(){
		InputStream stream = MailConnectorIvyTest.class.getResourceAsStream("testmails/mail-with-inline-and-attachments.eml");
		assertThat(stream).isNotNull();

		Message message = MailStoreService.loadMessage(stream);
		Collection<Part> parts;

		parts = MessageService.allParts(message, null, null);
		assertThat(parts).hasSize(10);

		parts = MessageService.allParts(message, null, MessageService.isImage("*"));
		assertThat(parts).hasSize(4);

		parts = MessageService.allParts(message, null, MessageService.isAttachment());
		assertThat(parts).hasSize(4);

		parts = MessageService.allParts(message, null, MessageService.isAttachment().and(MessageService.isImage("jpeg")));
		assertThat(parts).hasSize(2);

		parts = MessageService.allParts(message, null, MessageService.isInline());
		assertThat(parts).hasSize(1);
	}

	@Test
	public void testMailWithAttachedEmailAndInlinesAndAttachment(){
		InputStream stream = MailConnectorIvyTest.class.getResourceAsStream("testmails/mail-with-attached-email-and-inline-and-attachments.eml");
		assertThat(stream).isNotNull();

		Message message = MailStoreService.loadMessage(stream);
		Collection<Part> parts;

		parts = MessageService.allParts(message, null, null);
		assertThat(parts).hasSize(11);

		// includes the attachment in the attached email
		parts = MessageService.allParts(message, null, MessageService.isAttachment());
		assertThat(parts).hasSize(3);

		// does not include the attachment in the attached email
		parts = MessageService.allParts(message, MessageService.isAttachment().or(MessageService.isParent(false)), MessageService.isAttachment());
		assertThat(parts).hasSize(2);
	}
}
