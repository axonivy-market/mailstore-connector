package com.axonivy.connector.mailstore.test;

import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.axonivy.connector.mailstore.MailStoreBaseTest;

import ch.ivyteam.ivy.bpm.engine.client.BpmClient;
import ch.ivyteam.ivy.bpm.exec.client.IvyProcessTest;
import ch.ivyteam.ivy.environment.AppFixture;
import ch.ivyteam.ivy.environment.Ivy;

@Testcontainers
@IvyProcessTest
class MailStoreDemoIMAPTest extends MailStoreBaseTest {

	@BeforeAll
	static void setup(@TempDir Path conf) throws Exception {
		setupTest(conf);
	}
	
	@Test
	void imapReadTest(BpmClient client, AppFixture fixture) throws Exception {
		String userName = "user1@test.local";
		finishMailserverUserSetup(userName);
		configureDemo(fixture, userName);

		var result = callHandleEMailsIvyDemoProcess(client);
		Ivy.log().info(result);
	}

	@Override
	protected void configureDemo(AppFixture fixture, String userName) {
		configCommonVariable(fixture, userName);
		fixture.var(LOCAL_IMAP + ".protocol", "imap");
		var propertyPrefix = LOCAL_IMAP + ".properties.mail.";
		fixture.var(propertyPrefix + "imap.starttls.enable", "true");
		fixture.var(propertyPrefix + "imap.starttls.required", "true");
		fixture.var(propertyPrefix + "imap.auth", "true");
	}
}
