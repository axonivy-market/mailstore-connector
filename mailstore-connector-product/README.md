# Mailstore Connector

Unlock the potential of Axon Ivy's Mailstore connector to streamline your process automation endeavors, simplifying email management within your business processes. This versatile connector:

- Seamlessly integrates with both IMAP and POP3 mail stores.
- Ensures data security through robust SSL encryption.
- Accelerates your integration efforts with a user-friendly, ready-to-copy demo implementation.

Please be aware that the comprehensive feature set is exclusively accessible through the IMAP email protocol, while POP3 provides only fundamental functionality.

## Demo

Two demo processes are provided, which do the same thing once in an Ivy sub-process and once directly in a Java service function if you prefer. Both read from an IMAP inbox (for testing you can use a docker image like [virtua-sa/docker-mail-devel](https://github.com/virtua-sa/docker-mail-devel) or a public IMAP service like [Ethereal](https://ethereal.email/) and any IMAP mail client like [Thunderbird](https://www.thunderbird.net/de/).

The demo will read from the standard inbox messages containing the text `Test 999...` where 999 is a number. For every message it will save the message to a case document, extract all image parts and logs some information about them. So to see results, you should prepare such mails in the inbox. Messaages will not be deleted or moved to simplify testing.

Results will only be written to the Ivy logfile. In a later version, there will be perhaps a little GUI for this. Stay tuned!

## Usage

### From Java or Ivy Script

Use `com.axonivy.connector.mailstore.MailStoreService.messageIterator(String, String, String, boolean, Predicate<Message>)` to get an interator to new mails in a folder on a mail store. You can then iterate through the "new" mails in this folder dependeing on the given flags. If a destination folder is set, then mails which were handled successfully will be moved there. If the delete flag is set, then mails which were handled successfully will be deleted from the source folder.

A filter can be defined to match only specific mails. Standard filters to filter for parts of the subject, sender, recipients,... are provided directly but filters follow the standard Java `Predicate<Message>` interface and can be easily defined and combined with existing Java functionality (like `Predicate.and` or `Predicate.or`).

A typical call reading mails with a certain subject `Request 12345` from the `inbox` and moving then to an `archive` after successfull handkling would be created like this:

```java
MessageIteraor it = MailsStoreService.messageIterator("ethereal-imaps", "INBOX", "archive", true, MailStoreService.subjectMatches(".*Request [0-9]+.*")
```

When you are finished handling an Email successfully, you should cal the `handledMessage(boolean)` function, so the iterator will perform the configured action for this Email. Not calling this function or calling this function with `false` will leave the Email in the store and it will be delivered in the next run.

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
