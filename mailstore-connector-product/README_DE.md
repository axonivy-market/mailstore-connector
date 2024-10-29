# Mailstore Konnektor

Nutze den Axon Ivy Mailstore-Konnektor, um das E-Mail-Management in deinen Geschäftsprozessen zu vereinfachen. 

Dieser vielseitige Konnektor:

- lässt sich problemlos mit IMAP- und POP3-Mailstores verbinden.
- sichert deine Daten durch  SSL-Verschlüsselung.
- vereinfacht die Integration durch eine _ready-to-copy_ Demo-Implementierung.
  
Bitte beachte, dass in unserer Demo-Implementierung der vollständige Funktionsumfang ausschließlich über das IMAP-Protokoll verfügbar ist, für POP3 sind nur grundlegende Funktionen implementiert.

## Demo

Two demo processes are provided, which do the same thing once in an Ivy sub-process and once directly in a Java service function if you prefer. Both read from an IMAP inbox (for testing you can use a docker image like [virtua-sa/docker-mail-devel](https://github.com/virtua-sa/docker-mail-devel) or a public IMAP service like [Ethereal](https://ethereal.email/) and any IMAP mail client like [Thunderbird](https://www.thunderbird.net/de/).

The demo will read from the standard inbox messages containing the text `Test 999...` where 999 is a number. For every message it will save the message to a case document, extract all image parts and logs some information about them. So to see results, you should prepare such mails in the inbox. Messaages will not be deleted or moved to simplify testing.

Results will only be written to the Ivy logfile. In a later version, there will be perhaps a little GUI for this. Stay tuned!

## Usage

### From Java or Ivy Script

Use `com.axonivy.connector.mailstore.MailStoreService.messageIterator(String, String, String, boolean, Predicate<Message>, Comparator<Message>)` to get an interator to new mails in a folder on a mail store. You can then iterate through the "new" mails in this folder dependeing on the given flags. If a destination folder is set, then mails which were handled successfully will be moved there. If the delete flag is set, then mails which were handled successfully will be deleted from the source folder.

A filter can be defined to match only specific mails. Standard filters to filter for parts of the subject, sender, recipients,... are provided directly but filters follow the standard Java `Predicate<Message>` interface and can be easily defined and combined with existing Java functionality (like `Predicate.and` or `Predicate.or`).

Similar to filter, the sort follows the standard Java `Comparator<Message>` interface and can sort sent date, received date, subject,...

A typical call reading mails with a certain subject `Request 12345` from the `inbox` and moving then to an `archive` after successfull handling would be created like this:

```java
MessageIteraor it = MailsStoreService.messageIterator("ethereal-imaps", "INBOX", "archive", true, MailStoreService.subjectMatches(".*Request [0-9]+.*"), new MessageComparator())
```

When you are finished handling an Email successfully, you should call the `handledMessage(boolean)` function, so the iterator will perform the configured action for this Email. Not calling this function or calling this function with `false` will leave the Email in the store and it will be delivered in the next run.

### As a sub-process

All Email-handling can also be performed calling the provided sub-process `MailStoreConnector.handleMessages` and overriding the process to handle a single Email `MessageHandler.handleMessage`. Handling of mails will be marked as successful, when the overridden process returns with `handled=true` (and does not throw an error).

### Message handling

Handling a single message is easily supported by the `com.axonivy.connector.mailstore.MessageService.getAllParts(Message, boolean, Predicate<Part>)` and other convenience functions. The funtions support old style mails with text only and also MIME mails which can contain many different parts and even email-attachments. The basic idea is to pass a message and a filter to this function and then get back a list of `parts` matching the filter. Again, filters follow the standard Java `Predicate<Message>` interface and can be easily defined and combined with existing Java functionality (like `Predicate.and` or `Predicate.or`).

A typical call, extracting all images from an Email would look like this:

```java
Collection<Part> images = MessageService.getAllParts(message, false, MessageService.isImage("*"));
```

Additional convenience functions are provided to

* load and save messages
* extract all texts
* read binary content of a part

## Setup

Configure one or more mailstores in global variables. A mailstore is identified by a name and a
global variable section containing access information. The following example shows connection information
for a mailstore that should be accessible under the name `ethereal-imaps`. Put this variable block into your
project. At least `protocol`, `host`, `user` and `password` must be defined (note the encrypted `password`
and the value list for `protocol` which will later provide some input support in the engine cockpit).

If you want to see connect logs, use the `debug` switch. If your connection requires special settings, you
can set them in the `properties` section.

```yaml
Variables:
  mailstore-connector:
    ethereal-imaps:
      # [enum: pop3, pop3s, imap, imaps]
      protocol: 'imaps'
      # Host for store connection
      host: 'imap.ethereal.email'
      # Port for store connection (only needed if not default)
      port: -1
      # User name for store connection
      user: 'myname@ethereal.email'
      # Password for store connection
      # [password]
      password: '${encrypt:mypassword}'
      # show debug output for connection
      debug: true
      # Additional properties for store connection,
      # see https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
      properties:
          # mail.imaps.ssl.checkserveridentity: false
          # mail.imaps.ssl.trust: '*'
```

OAuth 2.0 Support: Azure client_credential/password grant flow

## Overview

This document outlines the steps to configure OAuth 2.0 support using the Azure client credentials grant flow.

### Configuration Steps
1. Ensure that the necessary properties are enabled for JavaMail to support OAuth 2.0. For more details, refer to the [JavaMail API documentation](https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#:~:text=or%20confidentiality%20layer.-,OAuth%202.0%20Support,-Support%20for%20OAuth).

```yaml
      properties:
          # only set below credential when you go with oauth2
          mail.imaps.auth.mechanisms: 'XOAUTH2'
          mail.imaps.sasl.enable: 'true'
          mail.imaps.sasl.mechanisms: 'XOAUTH2'
```

2. Add Credentials for Azure Authentication
Include your Azure credentials in the authentication configuration.
```yaml
      # Basic: username and password, AzureOauth2UserPasswordProvider: currently only support OAuth2 client credentials grant flow
      # com.axonivy.connector.oauth.BasicUserPasswordProvider for Basic Authentication
      # com.axonivy.connector.oauth.AzureOauth2UserPasswordProvider for AzureOauth2UserPasswordProvider
      userPasswordProvider: 'com.axonivy.connector.oauth.AzureOauth2UserPasswordProvider'
      
      # only set below credential when you go with oauth2
      # tenant to use for OAUTH2 request.
      # set the Azure Directory (tenant) ID, for application requests.
      tenantId: ''
      # Your Azure Application (client) ID, used for OAuth2 authentication
      appId: ''
      # Secret key from your applications "certificates & secrets" (client secret)
      secretKey: ''
      # for client_credentials: https://outlook.office365.com/.default
      scope: ''
      #[client_credentials]
      grantType: '
```

3. Provide a Complete YAML Configuration File
Ensure that a fully configured YAML file is available for the application.
```yaml
Variables:
  mailstore-connector:
    localhost-imap-azure-oauth2-authentication:
      # [enum: pop3, pop3s, imap, imaps]
      protocol: 'imap'
      # Host for store connection
      host: 'localhost'
      # Port for store connection (only needed if not default)
      port: -1
      # User name for store connection
      user: 'debug@localdomain.test'
      # Password for store connection
      # [password]
      password: ''
      # show debug output for connection
      debug: true
      # Additional properties for store connection,
      # see https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html
      properties:
          mail.imaps.ssl.checkserveridentity: false
          mail.imaps.ssl.trust: '*'
          # only set below credential when you go with oauth2
          mail.imaps.auth.mechanisms: 'XOAUTH2'
          mail.imaps.sasl.enable: 'true'
          mail.imaps.sasl.mechanisms: 'XOAUTH2'
      
      # Basic: username and password, AzureOauth2UserPasswordProvider: currently only support OAuth2 client credentials grant flow
      # com.axonivy.connector.oauth.BasicUserPasswordProvider for Basic Authentication
      # com.axonivy.connector.oauth.AzureOauth2UserPasswordProvider for AzureOauth2UserPasswordProvider
      userPasswordProvider: 'com.axonivy.connector.oauth.AzureOauth2UserPasswordProvider'
      
      # only set below credential when you go with oauth2
      # tenant to use for OAUTH2 request.
      # set the Azure Directory (tenant) ID, for application requests.
      tenantId: ''
      # Your Azure Application (client) ID, used for OAuth2 authentication
      appId: ''
      # Secret key from your applications "certificates & secrets" (client secret)
      secretKey: ''
      # for client_credentials: https://outlook.office365.com/.default
      scope: ''
      #[client_credentials/password]
      grantType: ''
  # login url microsoft zure
  azureOAuth:
    loginUrl: 'login.microsoftonline.com'
```

4. Set Up the Authentication Provider
Before calling the mailstore connector, you need to provide an authentication provider.
```java
  Class<?> clazz = Class.forName("com.axonivy.connector.oauth.AzureOauth2UserPasswordProvider");
	UserPasswordProvider userPasswordProvider = (UserPasswordProvider) clazz.getDeclaredConstructor().newInstance();
  MailStoreService.registerUserPasswordProvider(storeName, userPasswordProvider);
```