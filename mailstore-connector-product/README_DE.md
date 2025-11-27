# Mailstore Connector

Nutzen Sie das Potenzial des Mailstore-Konnektors von Axon Ivy, um Ihre
Prozessautomatisierung zu optimieren und die E-Mail-Verwaltung innerhalb Ihrer
Geschäftsprozesse zu vereinfachen. Dieser vielseitige Konnektor:

- Nahtlose Integration mit IMAP- und POP3-Mail-Speichern.
- Gewährleistet Datensicherheit durch robuste SSL-Verschlüsselung.
- Beschleunigen Sie Ihre Integrationsbemühungen mit einer benutzerfreundlichen,
  kopierfertigen Demo-Implementierung.

Bitte beachten Sie, dass der umfassende Funktionsumfang ausschließlich über das
IMAP-E-Mail-Protokoll zugänglich ist, während POP3 nur grundlegende Funktionen
bietet.

## Demo

Es werden zwei Demo-Prozesse bereitgestellt:

- Einer davon ist als Ivy-Unterprozess „ **” implementiert.**
- Eine als Java-Dienstfunktion „ **” implementierte Funktion.**

> Beide erfüllen dieselbe Aufgabe – wählen Sie den Integrationsstil, der Ihren
> Anforderungen entspricht.

Beide Demos verbinden sich mit einem IMAP-Posteingang von **** .

Zu Testzwecken können Sie Folgendes verwenden:

- Ein Docker-Container wie
  [`virtua-sa/docker-mail-devel`](https://github.com/virtua-sa/docker-mail-devel)
- Ein öffentlicher IMAP-Testdienst wie [Ethereal](https://ethereal.email/)
- Jeder IMAP-fähige Client wie [Thunderbird](https://www.thunderbird.net/de/)

Die Demo liest Nachrichten aus dem Standard-Posteingang, die den folgenden Text
enthalten: `Test 999` (wobei `999` eine beliebige Zahl ist).

Für jede übereinstimmende Meldung gilt Folgendes:

- Speichert die Nachricht als Dokument im Fall „ **“.**
- Extrahiert alle Bildteile aus „ **“**
- Protokolliert relevante Metadaten im Ivy-Protokoll „ **“.**

> Um dies zu testen, bereiten Sie solche Nachrichten im Posteingang vor.\
> Die Nachrichten befinden sich unter **und werden nicht gelöscht oder
> verschoben**, damit die Tests wiederholbar und sicher sind.

### 📝 Ausgabe & Ausblick

- Alle Ausgaben werden in das Ivy-Protokoll „ **“ geschrieben:**.
- In einer zukünftigen Version wird möglicherweise eine einfache GUI hinzugefügt
  – bleiben Sie dran!

## Verwendung

### Aus Java oder Ivy Script

1. Verwenden Sie
   `com.axonivy.connector.mailstore.MailStoreService.messageIterator(String,
   String, String, boolean, Predicate<message>, Comparator<message>)`, um einen
   Iterator für neue E-Mails in einem bestimmten Ordner eines Mailspeichers zu
   erhalten. Anschließend können Sie diese Nachrichten basierend auf den
   bereitgestellten Filter- und Konfigurationsflags
   durchlaufen.</message></message>
  - Wenn ein Zielordner „ **“ angegeben ist (** ), werden erfolgreich
    bearbeitete Nachrichten dorthin verschoben ( **) (** ).
  - Wenn das Löschflag „ **“** gesetzt ist, werden erfolgreich bearbeitete
    Nachrichten stattdessen aus dem Quellordner gelöscht **** .


2. Es kann ein Filter definiert werden, der nur bestimmte Nachrichten auswählt.
   Es stehen Standardfilter zur Verfügung, um Teile des Betreffs **,**, **, des
   Absenders**, **, der Empfänger** und mehr auszuwählen.
  - Filter basieren auf der Standard-Java- `-Schnittstelle Predicate<message>`
    und können mithilfe von Standard-Java-Funktionen wie `Predicate.and(...)`
    oder `Predicate.or(...)` einfach definiert und kombiniert werden.</message>


3. Ähnlich wie der Filter folgt auch die Sortierung der Standard-Java-
   `-Schnittstelle Comparator<message>` und kann nach Sendedatum, Empfangsdatum,
   Betreff usw. sortieren.</message>

4. Ein typischer Aufruf, der E-Mails mit einem bestimmten Betreff wie „ `Request
   12345` ” aus dem Posteingang „ `” im Ordner „` ” liest und sie nach
   erfolgreicher Verarbeitung in das Archiv „ `” im Ordner „` ” verschiebt, kann
   wie folgt geschrieben werden:

```java
MessageIteraor it = MailsStoreService.messageIterator("etherealImaps", "INBOX", "archive", true, MailStoreService.subjectMatches(".*Request [0-9]+.*"), new MessageComparator())
```

Wenn Sie eine E-Mail erfolgreich bearbeitet haben, sollten Sie die Funktion „
`handledMessage(boolean)` ” aufrufen.\
Dadurch wird der Iterator angewiesen, die konfigurierte Aktion (z. B.
Verschieben oder Löschen) für diese Nachricht auszuführen.

Wenn Sie **statt** aufrufen oder wenn Sie es mit `false` aufrufen, bleibt die
Nachricht im Speicher und wird beim nächsten Durchlauf erneut zugestellt.


### Als Teilprozess

Die gesamte E-Mail-Verarbeitung kann auch durch Aufruf des bereitgestellten
Unterprozesses `MailStoreConnector.handleMessages` und Überschreiben des
Prozesses zur Verarbeitung einer einzelnen E-Mail `MessageHandler.handleMessage`
durchgeführt werden. Die Verarbeitung von E-Mails wird als erfolgreich markiert,
wenn der überschriebene Prozess mit `handled=true` zurückkehrt (und keinen
Fehler auslöst).

### Nachrichtenverarbeitung

Die Verarbeitung einer einzelnen Nachricht wird durch die Funktionen „ `“,
„com.axonivy.connector.mailstore.MessageService.getAllParts(Message, boolean,
Predicate<part>)“, „` “ und andere praktische Funktionen unterstützt. Die
Funktionen unterstützen sowohl E-Mails im alten Stil mit reinem Text als auch
MIME-E-Mails, die viele verschiedene Teile und sogar E-Mail-Anhänge enthalten
können. Die Grundidee besteht darin, eine Nachricht und einen Filter an diese
Funktion zu übergeben und dann eine Liste von `Teilen` zurückzubekommen, die dem
Filter entsprechen. Auch hier folgen die Filter der Standard-Java-
`-Schnittstelle Predicate<message>` und können einfach definiert und mit
bestehenden Java-Funktionen kombiniert werden (wie `Predicate.and` oder
`Predicate.or`).</message></part>

Ein typischer Aufruf zum Extrahieren aller Bilder aus einer E-Mail würde wie
folgt aussehen:

```java
Collection<Part> images = MessageService.getAllParts(message, false, MessageService.isImage("*"));
```

Zusätzliche Komfortfunktionen werden bereitgestellt, um

* Laden und Speichern von Nachrichten
* Extrahieren Sie alle Texte.
* Binärinhalt eines Teils lesen

## Setup

Konfigurieren Sie einen oder mehrere Mailstores in globalen Variablen. Ein
Mailstore wird durch einen Namen und einen globalen Variablenabschnitt mit
Zugriffsinformationen identifiziert. Das folgende Beispiel zeigt die
Verbindungsinformationen für einen Mailstore, der unter dem Namen
`etherealImaps` zugänglich sein sollte. Fügen Sie diesen Variablenblock in Ihr
Projekt ein. Mindestens `protocol`, `host`, `user` und `password` müssen
definiert sein (beachten Sie das verschlüsselte `password` und die Werteliste
für `protocol`, die später einige Eingabehilfen im Engine-Cockpit bereitstellen
wird).

Wenn Sie Verbindungsprotokolle anzeigen möchten, aktivieren Sie den
Debug-Schalter „ `“`.\
Wenn Ihre Verbindung spezielle Einstellungen erfordert, können Sie diese im
Abschnitt „ `“` festlegen.


```yaml
Variables:
  mailstoreConnector:
    etherealImaps:
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

OAuth 2.0-Unterstützung: Azure client_credential/password Grant Flow

## Übersicht

Dieses Dokument beschreibt die Schritte zur Konfiguration der OAuth
2.0-Unterstützung mithilfe des
Azure-Client-Anmeldeinformations-Gewährungsflusses.

### Konfigurationsschritte
1. Stellen Sie sicher, dass die erforderlichen Eigenschaften für JavaMail
   aktiviert sind, damit OAuth 2.0 unterstützt wird. Weitere Informationen
   finden Sie in der
   [JavaMail-API-Dokumentation](https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#:~:text=or%20confidentiality%20layer.-,OAuth%202.0%20Support,-Support%20for%20OAuth).

```yaml
      properties:
          # only set below credential when you go with oauth2
          mail.imaps.auth.mechanisms: 'XOAUTH2'
          mail.imaps.sasl.enable: 'true'
          mail.imaps.sasl.mechanisms: 'XOAUTH2'
```

2. Hinzufügen von Anmeldeinformationen für die Azure-Authentifizierung Fügen Sie
   Ihre Azure-Anmeldeinformationen in die Authentifizierungskonfiguration ein.
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

3. Stellen Sie eine vollständige YAML-Konfigurationsdatei bereit Stellen Sie
   sicher, dass eine vollständig konfigurierte YAML-Datei für die Anwendung
   verfügbar ist.
```yaml
Variables:
  mailstoreConnector:
    localhostImapAzureOauth2Authentication:
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
> [!HINWEIS] Der variable Pfad `mailstore-connector` wird ab Version 13 in
> `mailstoreConnector` umbenannt.

4. Einrichten des Authentifizierungsanbieters Bevor Sie den Mailstore-Konnektor
   aufrufen, müssen Sie einen Authentifizierungsanbieter angeben.
```java
  Class<?> clazz = Class.forName("com.axonivy.connector.oauth.AzureOauth2UserPasswordProvider");
	UserPasswordProvider userPasswordProvider = (UserPasswordProvider) clazz.getDeclaredConstructor().newInstance();
  MailStoreService.registerUserPasswordProvider(storeName, userPasswordProvider);
```
