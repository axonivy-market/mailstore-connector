# Mailstore Anschluss

Entsperr das Potential #Axon Efeus Mailstore Anschluss zu #windschlüpfig machen
eure Arbeitsgang Automatisierung Bemühungen, vereinfachend #Email Management
innerhalb euren dienstlichen Arbeitsgängen. Dieser vielseitiger Anschluss:

- #Bruchlos integriert mit #beide IMAP und POP3 Post Vorräte.
- Sichert #Daten Sicherheit durch stabil SSL Verschlüsselung.
- Beschleunigt eure Integration Anstrengungen mit einem Nutzer-freundlich,
  bereit-zu-Kopie Demo Ausführung.

Bitte sei bewusst dass der umfassende Charakterzug Apparat ist exklusiv
erreichbar durch der IMAP #Email Protokoll, #während POP3 versieht einzige
grundsätzliche Funktionalität.

## Demo

Zwei Demo Arbeitsgänge sind versehen:

- #Man implementierte da ein **Efeu Ersatz-Arbeitsgang**
- #Man implementierte da ein **#Java #bespringen Aufgabe**

> #Beide aufführen ebensolchen gleichen Task – wählt aus whichever Integration
> Stil passt eure Notwendigkeiten.

Beide Demos koppeln zu #ein **IMAP inbox**.

Für testen Zwecke, du kannst benutzen:

- Ein Hafenarbeiter Behälter wie
  [`virtua-sa/Hafenarbeiter-Post-devel`](https://github.com/virtua-sa/docker-mail-devel)
- Ein öffentliches IMAP Probe Bedienung mag [Ätherisch](https://ethereal.email/)
- #Irgendein IMAP-fähiger Kunde wie
  [Donnervogel](https://www.thunderbird.net/de/)

Die Demo liest Meldungen von das tarifliches inbox jener zügeln den Text:
`Klausur 999` (#wo `999` ist irgendwelche Nummer).

Für jede #zusammenpassend Meldung, es:

- Speichert da die Meldung eine **Fall Dokument**
- Extrakte alle **Image Teile**
- #Loggen #relevant #Metadaten zu #der **Efeu #loggen**

> Zu testen ihm, bereite vor herein solche Meldungen die inbox.\
> Meldungen sind **nicht gestrichen oder begeben**, zu Unterhalt Probe
> repeatable und zuverlässig.

### 📝 Ausgabe & Aussicht

- Alle Ausgabe ist #anschreiben die **Efeu #loggen**.
- Eine simple #grafische Benutzeroberfläche darf sein zugefügt in einer
  künftigen Version — Stag stimmte ab!

## Gebrauch

### Von #Java oder Efeu #Skripten

1. Nutzung
   `com.axonivy.Anschluss.mailstore.MailStoreService.messageIterator(Schnur,
   Schnur, Schnur, aussagenlogisch, Satzaussage<message>, #Komparator<message>)`
   zu bekommen ein iterator über neu #Email in einem spezifischen Ordner von
   einen Post Vorrat. Du kannst dann durch diese Meldungen #iterieren gegründet
   auf dem #versehen Filter und Konfiguration Fahnen.</message></message>
  - Ob ein **Reiseziel Ordner** ist präzisiert, Meldungen dass sind erfolgreich
    bedient wollen sein **begeben** dort.
  - Ob das **streicht Fahne** ist gesetzt, erfolgreich bediente Meldungen wollen
    sein **gestrichen** von den Quelle Ordner stattdessen.


2. Ein Filter kann sein definiert zu passen einzige #Spezifikum Meldungen.
   Tarifliche Filter sind versehen zu passen Teile von die **Sujet**,
   **Absender**, **Empfänger**, und More.
  - Filtert #sein gegründet auf #der #tariflich #Java `Satzaussage<message>`
    #einbinden und kann sein sicher definiert und verbunden benutzend wie
    tarifliche #Java Funktionalität `Satzaussage.Und(...)` Oder
    `Satzaussage.Oder(...)`.</message>


3. Gleichartig zu filtern, die Sorte folgt #der #tariflich #Java
   `#Komparator<message>` #einbinden und kann ordnen #hingerissen Datum,
   anerkanntes Datum, Sujet,...</message>

4. Ein typischer Anruf dass liest #Email mit einem spezifischen Sujet mag `Bitte
   12345` von die `inbox` Ordner und begibt jene zu ein `Archiv` Ordner nach
   erfolgreich Verarbeitung kann sein geschrieben folgendermaßen:

```java
MessageIteraor it = MailsStoreService.messageIterator("etherealImaps", "INBOX", "archive", true, MailStoreService.subjectMatches(".*Request [0-9]+.*"), new MessageComparator())
```

Wann du hast erfolgreich bedient eine #Email, du solltest rufen die
`handledMessage(aussagenlogisch)` Aufgabe.\
Dies informiert das iterator zu aufführen das konfiguriert Aktion (#z.B., begib
oder streichen) für jene Meldung.

Ob du tust **nicht** rufst diese Aufgabe, oder ob du rufst ihm mit `falsch`, die
Meldung will in dem Vorrat verharren und will sein angeliefert nochmal während
dem nächsten Lauf.


### Da einen Ersatz-verarbeite

Alle #Email-bedienen kann auch sein #aufführen rufen den #versehen
Ersatz-Arbeitsgang `MailStoreConnector.handleMessages` Und #außer Kraft setzend
der Arbeitsgang zu bedienen eine ledige #Email `MessageHandler.handleMessage`.
Bedienen von #Email wollen sein zensiert da erfolgreich, als die overridden
Arbeitsgang Rückgaben mit `bedienten=wahr` (und werfen nicht einen Fehler).

### Meldung Handing

Bedienend eine ledige Meldung ist sicher mal unterstützt die
`com.axonivy.Anschluss.mailstore.MessageService.getAllParts(Meldung,
aussagenlogisch, Satzaussage<part>)` und anderen Annehmlichkeit Aufgaben. Das
funtions unterstützen alte Stil #Post mit Text nur und auch MIMEN #Post #welche
können zügeln #viele verschiedene Teile und gleichmäßige #Email-Anfügungen. Die
einfache Idee ist zu abspielen eine Meldung und ein Filter zu dieser Aufgabe und
dann zurückbekommen eine Liste von `Teile` #zusammenpassend den Filter. Nochmal,
Filter folgen #der #tariflich #Java `Satzaussage<message>` #einbinden und kann
sein sicher definiert und verquickt mit #existierend #Java Funktionalität
(#mögen `Satzaussage.Und` oder `Satzaussage.Oder`).</message></part>

Ein typischer Anruf, gewinnend alle Images von einer #Email wollte #aussehen
dies:

```java
Collection<Part> images = MessageService.getAllParts(message, false, MessageService.isImage("*"));
```

Zuzügliche Annehmlichkeit Aufgaben sind versehen zu

* Lade und speichern Meldungen
* Extrakt alle Texte
* Lies dualen Inhalt von einem Teil

## Einrichtung

Konfigurier eins oder More mailstores in global Variablen. Ein mailstore ist
identifiziert #bei einem Namen und eine globale Variable Sektion zügelnd Zugang
Auskunft. Das folgende Beispiel Vorstellungen Zusammenhang Auskunft für eine
mailstore jener sollte sein erreichbar unter dem Namen `etherealImaps`. Leg
diesen variablen Block hinein eure Projekt. Mindestens `Protokoll`, `Gastgeber`,
`Nutzer` und `Passwort` muss sein definiert (beachte das #verschlüsselt
`Passwort` und die Wert Liste für `Protokoll` #welche will nachher versehen
einige Input Unterstützung in die Motor Pilotenkabine).

Ob du möchtest Zusammenhang Loge sehen, aktiviert die `entwanzen` Schalter.\
Ob eure Zusammenhang bedürft spezielle Lagen, du kannst definieren jene herein
die `#Besitz` Sektion.


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

OAuth 2.0 Unterstütz: Himmelblauer Kunde_#Berechtigungsnachweis/Passwort Grant
Strömung

## Überblick

Dieses Dokument skizziert die Stufen zu konfigurieren OAuth 2.0 Unterstützung
benutzend den Himmelblauen Kunden #Berechtigungsnachweis Grant Strömung.

### Konfiguration Stufen
1. Sicher dass die nötigen #Besitz sind aktiviert für JavaMail zu unterstützen
   OAuth 2.0. Für #mehr Details, übergebt zu die [JavaMail API
   Dokumentation](https://javaee.github.io/javamail/docs/api/com/sun/mail/imap/package-summary.html#:~:text=or%20confidentiality%20layer.-,OAuth%202.0%20Support,-Support%20for%20OAuth).

```yaml
      properties:
          # only set below credential when you go with oauth2
          mail.imaps.auth.mechanisms: 'XOAUTH2'
          mail.imaps.sasl.enable: 'true'
          mail.imaps.sasl.mechanisms: 'XOAUTH2'
```

2. Füg zu #Berechtigungsnachweis für Himmelblau Authentifizierung Schließt ein
   eure Himmelblauen #Berechtigungsnachweis in die Authentifizierung
   Konfiguration.
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

3. Versieh ein Vollständiges YAML Konfiguration Datei Sichert dass eine vollauf
   konfigurierte YAML Datei ist verfügbar für den Antrag.
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
> [!BEACHTE] Den variablen Pfad `mailstore-Anschluss` ist #umbenennen zu
> `mailstoreConnector` von 13.

4. Stell auf den Authentifizierung Provider Vor rufen den mailstore Anschluss,
   du brauchst zu versehen einen Authentifizierung Provider.
```java
  Class<?> clazz = Class.forName("com.axonivy.connector.oauth.AzureOauth2UserPasswordProvider");
	UserPasswordProvider userPasswordProvider = (UserPasswordProvider) clazz.getDeclaredConstructor().newInstance();
  MailStoreService.registerUserPasswordProvider(storeName, userPasswordProvider);
```
