# mailstore-connector

Connect to IMAP and POP3 mail stores optionally via SSL and handle new mails in an easy way.

## Setup

Configure one or more mailstores in global variables. A mailstore is identified by a name and a
global variable section containing access information. The following example shows connection information
for a mailstore that should be accessible under the name `ethereal-imaps`. Put this variable block into your
project. At least `protocol`, `host`, `user` and `password` must be defined (Note, the encrypted `password`
and the value list for `protocol` which will later provide some input support in the engine cockpit.

```
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

## Usage

### From Java or Ivy Script

Use `com.axonivy.market.mailstore.connector.MailStoreService.messageIterator(String, String, String, boolean, Predicate<Message>)` to get an interator to new mails in a folder on a mail store. You can then iterate through the "new" mails in this folder dependeing on the given flags. If a destination folder is set, then mails which were handled successfully will be moved there. If the delete flag is set, then mails which were handled successfully will be deleted from the source folder.

A filter can be defined to match only specific mails. Standard filters to filter for parts of the subject, sender, recipients,... are provided directly but filters follow the standard Java `Predicate<Message>`interface and can be easily defined and combined with existing Java functionality (like `Predicate.and` or `Predicate.or`).

## Demo






