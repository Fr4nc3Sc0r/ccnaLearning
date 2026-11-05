# 09 - Manutenzione ed evoluzione

Guida pratica per chi deve **aggiungere contenuti**, **correggere errori** o **migliorare** il
programma. Ogni sezione indica esattamente quali file toccare.

## Indice

1. [Preparare l'ambiente](#1-preparare-lambiente)
2. [Aggiungere domande (anche senza saper programmare)](#2-aggiungere-domande)
3. [Correggere o eliminare una domanda](#3-correggere-o-eliminare-una-domanda)
4. [Aggiungere o modificare una pagina del cheat sheet](#4-aggiungere-o-modificare-una-pagina-del-cheat-sheet)
5. [Cambiare i parametri del programma](#5-cambiare-i-parametri-del-programma)
6. [Aggiornare il programma a un nuovo blueprint (es. v2.0)](#6-aggiornare-il-programma-a-un-nuovo-blueprint)
7. [Aggiungere un nuovo tipo di domanda](#7-aggiungere-un-nuovo-tipo-di-domanda)
8. [Aggiungere una variante al generatore di subnetting](#8-aggiungere-una-variante-al-generatore-di-subnetting)
9. [Aggiungere una nuova modalità d'esame o una schermata](#9-aggiungere-una-nuova-modalità-o-una-schermata)
10. [Verifiche prima di pubblicare una modifica](#10-verifiche-prima-di-pubblicare-una-modifica)
11. [Problemi comuni e soluzioni](#11-problemi-comuni-e-soluzioni)
12. [Limiti noti e idee di miglioramento](#12-limiti-noti-e-idee-di-miglioramento)
13. [Convenzioni del codice](#13-convenzioni-del-codice)
14. [Versioni e rilasci](#14-versioni-e-rilasci)

---

## 1. Preparare l'ambiente

| Cosa | Perché | Come |
|---|---|---|
| **JDK 17+** | compilare ed eseguire | es. Eclipse Temurin da adoptium.net; verifica con `java -version` e `javac -version` |
| **VS Code** + *Extension Pack for Java* | modificare e avviare comodamente | il progetto contiene `.vscode/` già configurato (F5 avvia, Ctrl+Shift+B crea il JAR) |
| **Git** | tenere traccia delle modifiche | `git pull` per aggiornare, `git commit` / `git push` per pubblicare |
| *Maven* (facoltativo) | build alternativa | `mvn package` → `target/ccna-examlab.jar` |

Comandi principali dalla cartella del progetto:

```bash
./build.sh                                  # Windows: build.bat   → compila e crea dist/ccna-examlab.jar
java -jar dist/ccna-examlab.jar             # avvia
java -jar dist/ccna-examlab.jar --validate  # controlla le domande senza aprire finestre
```

---

## 2. Aggiungere domande

Ci sono **due strade**.

### Strada A — Domande personali (nessuna compilazione)

1. Avvia il programma una volta: crea la cartella `~/.ccna-examlab/questions/`
   (su Windows `C:\Users\<nome>\.ccna-examlab\questions\`; il percorso esatto è nel menu
   **File → Cartella dati utente…**).
2. Crea in quella cartella un file di testo con estensione `.txt` (es. `mie-domande.txt`), salvato in
   **UTF-8** (in Blocco note: *Salva con nome → Codifica: UTF-8*).
3. Scrivi le domande nel formato descritto sotto.
4. Riavvia il programma: le domande vengono caricate. Se qualcosa è sbagliato compare un avviso con
   il nome del file e la riga del blocco.

### Strada B — Domande incluse nel programma (per tutti gli utenti)

1. Apri il file del dominio giusto in `src/main/resources/questions/` (es. `03_ip_connectivity.txt`)
   e aggiungi i blocchi in fondo. Oppure crea un nuovo file e **aggiungilo a `index.txt`**, altrimenti
   non verrà letto.
2. Esegui `./build.sh`: la build termina con la validazione e mostra eventuali errori.
3. Prova la domanda nel programma (vedi §10).

### Il formato, campo per campo

```text
[Q]
id: IPC-062
domain: 3
type: SINGLE
text:
Testo della domanda in inglese.
Può stare su più righe.
exhibit:
R1#show ip interface brief
Interface              IP-Address      OK? Method Status                Protocol
GigabitEthernet0/0     10.0.0.1        YES manual up                    up
options:
A) Prima opzione
B) Seconda opzione
C) Terza opzione
D) Quarta opzione
answer: C
explanation:
Spiegazione in italiano. Puoi usare `codice` e **grassetto**.
reference: Exam topic 3.1 - Interpret the components of routing table
[/Q]
```

| Campo | Obbligatorio | Regole |
|---|---|---|
| `[Q]` … `[/Q]` | sì | aprono e chiudono ogni domanda, da soli sulla riga |
| `id` | sì | **unico** in tutta la banca. Convenzione: `NF-`, `NA-`, `IPC-`, `IPS-`, `SEC-`, `AUT-` + numero progressivo. Non cambiarlo dopo la pubblicazione: le statistiche degli utenti sono legate all'ID |
| `domain` | sì | `1`…`6` (oppure `1.0` o il nome, es. `IP_CONNECTIVITY`) |
| `type` | sì | `SINGLE`, `MULTI`, `DRAG`, `FILL` |
| `text` | sì | il testo; per MULTI "(Choose two.)" viene aggiunto da solo se manca |
| `exhibit` | no | testo monospazio (output di comandi, JSON, schemi). Gli spazi vengono mantenuti |
| `options` | SINGLE/MULTI | una per riga, `A) testo` (o `A. testo`). Una riga senza lettera continua l'opzione precedente |
| `answer` | sì (tranne DRAG) | SINGLE: `B` · MULTI: `A, C` · FILL: `30` o alternative `192.168.1.0 \| 192.168.1.0/24` |
| `items` | DRAG | una riga per elemento: `elemento => categoria corretta` |
| `targets` | no (DRAG) | categorie "distrattrici" aggiuntive: `Opzione X \| Opzione Y` |
| `explanation` | consigliato | in italiano; **non citare mai le lettere** ("l'opzione C…") perché le opzioni vengono mescolate |
| `reference` | consigliato | l'exam topic del blueprint |
| `shuffle` | no | `no` per tenere fisso l'ordine delle opzioni (solo se un'opzione dice "All of the above" o cita altre lettere) |

### Esempi per ogni tipo

**MULTI**
```text
[Q]
id: SEC-046
domain: 5
type: MULTI
text:
Which two protocols provide encrypted remote management access?
options:
A) Telnet
B) SSH
C) HTTP
D) HTTPS
answer: B, D
explanation:
SSH e HTTPS cifrano la sessione; Telnet e HTTP trasmettono in chiaro.
reference: Exam topic 4.8 / 2.8
[/Q]
```

**FILL**
```text
[Q]
id: NF-064
domain: 1
type: FILL
text:
How many usable hosts are available in a /28 subnet? (Enter a number.)
answer: 14
explanation:
2^4 - 2 = 14.
reference: Exam topic 1.6
[/Q]
```

**DRAG**
```text
[Q]
id: IPS-040
domain: 4
type: DRAG
text:
Match each protocol to its default port.
items:
SSH => 22
DNS => 53
NTP => 123
targets: 80 | 443
explanation:
...
reference: Exam topic 4.x
[/Q]
```

### Le regole d'oro (e gli errori più frequenti)

1. **Le parole chiave vanno a inizio riga**, senza spazi prima. Al contrario, se nel testo o
   nell'exhibit c'è una riga che *inizia* con una parola chiave seguita da due punti (es. un file YAML
   con `text: ciao`), **indentala** con almeno uno spazio, altrimenti verrà scambiata per un nuovo campo.
2. Non dimenticare `[/Q]`: l'errore "blocco non chiuso" indica la riga dove inizia la domanda.
3. Per SINGLE metti **una** lettera, per MULTI **almeno due**.
4. Le lettere in `answer` devono corrispondere a opzioni esistenti.
5. Salva in **UTF-8**, altrimenti le lettere accentate diventano simboli strani.
6. Nelle FILL prevedi tutte le forme accettabili (`255.255.255.0 | /24`?) — il confronto ignora solo
   maiuscole e spazi superflui.
7. Controlla che la domanda appartenga al **blueprint in vigore** (oggi v1.1). Per la qualità: una sola
   risposta inequivocabilmente corretta, distrattori plausibili, spiegazione che dica *perché* le altre
   sono sbagliate.

### Bilanciare la banca

Gli esami predefiniti e casuali pescano in proporzione ai pesi (20/20/25/10/15/10). Per avere esami
vari, mantieni il numero di domande per dominio **almeno** proporzionale ai pesi. Controlla i numeri con
`--validate`.

---

## 3. Correggere o eliminare una domanda

- **Correggere**: modifica il blocco mantenendo lo **stesso ID** (lo storico resta coerente).
- **Eliminare**: cancella l'intero blocco da `[Q]` a `[/Q]`. Le statistiche di quell'ID restano in
  `stats.tsv` degli utenti ma vengono semplicemente ignorate.
- **Non riusare** l'ID di una domanda eliminata per una domanda diversa.

Attenzione: aggiungere o eliminare domande **ridistribuisce** gli esami predefiniti A, B, C… (vedi
[algoritmo 3](08-algoritmi.md#3-suddivisione-negli-esami-predefiniti)).

---

## 4. Aggiungere o modificare una pagina del cheat sheet

1. Crea un file `.html` in `src/main/resources/reference/` (puoi copiare una pagina esistente come modello).
2. Aggiungi una riga a `src/main/resources/reference/index.txt`: `nomefile.html|Titolo nella lista`.
   L'ordine delle righe è l'ordine della lista.
3. Ricompila.

Il visualizzatore di Swing (`JEditorPane`) supporta solo **HTML 3.2 e CSS di base**: usa `h1`, `h2`, `p`,
`ul/ol`, `table` (con `border="0" cellspacing="0" width="100%"`), `code`, `pre`, `b`, `i` e la classe
`tip` per i riquadri evidenziati. Niente JavaScript, flexbox, immagini esterne o CSS moderno. Gli stili
comuni sono definiti in `ReferencePanel` (regole `css.addRule(...)`).

---

## 5. Cambiare i parametri del programma

| Parametro | File | Elemento | Valore attuale |
|---|---|---|---|
| Soglia di superamento | `model/ExamResult.java` | `PASSING_SCORE` | 825 |
| Scala del punteggio | `model/ExamResult.java` | `MIN_SCORE`, `MAX_SCORE` | 300, 1000 |
| Minuti per domanda | `engine/ExamFactory.java` | `MINUTES_PER_QUESTION` | 1,2 |
| Dimensione degli esami predefiniti | `engine/ExamFactory.java` | `PRESET_SIZE` | 70 (→ esami da ~75) |
| Suddivisione fissa degli esami | `engine/ExamFactory.java` | `PRESET_SEED` | 200301 (cambiandolo cambia la composizione di A, B…) |
| Domande dell'esame completo casuale | `ui/ExamsPanel.java` | `startFullRandom()` | 100 |
| Domande del drill di subnetting | `ui/ExamsPanel.java` | `startSubnetDrill()` | 20 |
| Colori e caratteri | `ui/Ui.java` | costanti `HEADER_BG`, `BASE`, … | — |
| Soglie colore delle barre dominio | `ui/ResultsPanel.java`, `ui/HistoryPanel.java` | `82.5` e `60` | 82,5% / 60% |
| Avviso tempo in esaurimento | `ui/ExamPanel.java` | `updateTimer()` | ultimi 300 s |
| Dimensioni della finestra | `ui/MainFrame.java` | `setSize`, `setMinimumSize` | 1240×840 / 980×680 |
| Nome e versione | `app/AppContext.java` + `pom.xml` | `APP_NAME`, `VERSION` | CCNA ExamLab 1.0.0 |
| Cartella dati | riga di comando | `-Dccna.examlab.home=...` | `~/.ccna-examlab` |

---

## 6. Aggiornare il programma a un nuovo blueprint

Il blueprint in vigore è codificato in un solo punto: l'enum `model/Domain.java`. Il blueprint **v2.0**
(da febbraio 2027) ha **cinque** domini con nomi e pesi diversi, quindi l'aggiornamento richiede:

1. **`Domain.java`**: sostituire i valori dell'enum con i nuovi domini (codice, titolo, peso). Se si
   vuole supportare entrambe le versioni, una strada è aggiungere un enum o un file di configurazione per
   blueprint e far scegliere la versione all'utente.
2. **File delle domande**: aggiornare il campo `domain` di ogni domanda, eliminare quelle fuori
   programma e aggiungerne di nuove (es. agentic AI, storm control, RA guard, OSPFv3, SFTP/SCP, record DNS).
3. **`index.txt`** e i nomi dei file, se si riorganizzano per dominio.
4. **Cheat sheet**: `exam_tips.html` (pesi e formato) e le pagine degli argomenti cambiati.
5. **README** principale (tabella dei domini).
6. **Compatibilità dello storico**: `history.tsv` salva i nomi dei domini (es. `IP_CONNECTIVITY`). Se un
   nome sparisce dall'enum, le righe che lo contengono non sono più leggibili e vengono saltate (con un
   messaggio in console). Per conservarle bisogna convertire il file o rendere `parseHistoryLine`
   tollerante ai domini sconosciuti.

---

## 7. Aggiungere un nuovo tipo di domanda

Esempio: un tipo `ORDER` (mettere in ordine dei passi). File da toccare, in ordine:

| # | File | Cosa fare |
|---|---|---|
| 1 | `model/QuestionType.java` | aggiungere il valore con la sua etichetta |
| 2 | `model/Question.java` | eventuali nuovi campi + Builder; casi in `isAnswered`, `isCorrect`, `getCorrectAnswerText`; controlli in `Builder.build()` |
| 3 | `model/Response.java` | spazio per memorizzare la nuova risposta (es. una lista ordinata) |
| 4 | `data/QuestionParser.java` | leggere i nuovi campi in `build()` (e aggiungerli alla regex `KEY` se servono nuove chiavi) |
| 5 | `ui/QuestionView.java` | un metodo `buildOrder(...)` e il relativo `case` in `display()`, con la visualizzazione in revisione |
| 6 | documentazione | questo file, `03-classi-model.md`, README (formato) |
| 7 | domande d'esempio | almeno una domanda per provare il tipo |

Suggerimento: un vero drag and drop con il mouse si può realizzare con `TransferHandler` di Swing,
mantenendo identico il modello (`Response.setDragAnswer`).

---

## 8. Aggiungere una variante al generatore di subnetting

1. In `data/SubnetQuestionGenerator.java` scrivi un nuovo metodo privato, ad esempio
   `private Question wildcardForPrefix()`, prendendo come modello `hostsPerSubnet()`:
   - usa `base(QuestionType.FILL)` o `base(QuestionType.SINGLE)`;
   - calcola la soluzione con `IpUtils`;
   - imposta `text`, le risposte (`accept` o `option`/`correct`) e `explanation`.
2. In `generateOne()` aumenta il numero delle varianti (`random.nextInt(7)`) e aggiungi il `case`.
3. Verifica la correttezza generando molte domande e controllando che la risposta indicata sia accettata
   (lo stesso controllo fatto in fase di sviluppo su 3000 domande). Un modo semplice è un piccolo
   programma di prova:

```java
SubnetQuestionGenerator g = new SubnetQuestionGenerator(new Random(1));
for (int i = 0; i < 3000; i++) {
    Question q = g.generateOne();
    Response r = new Response();
    if (q.getType() == QuestionType.FILL) r.setFillText(q.getAcceptedAnswers().get(0));
    else for (int c : q.getCorrectOptions()) r.select(c, true);
    if (!q.isCorrect(r)) System.out.println("ERRORE: " + q.getText());
}
```

---

## 9. Aggiungere una nuova modalità o una schermata

**Nuova modalità nel menu "Esami"** (es. "Solo exhibit"):
1. Se serve un nuovo criterio di selezione aggiungi un valore a `engine/QuestionFilter.java` e il caso in
   `ExamFactory.matches()`.
2. In `ui/ExamsPanel.java` aggiungi una riga con `addMode(other, y, "Titolo", "Descrizione", azione)` e un
   metodo che costruisce un `ExamConfig` e chiama `starter.accept(ctx.getFactory().custom(cfg))`.

**Nuova scheda nella home**: crea una classe che estende `JPanel` in `ui/` e aggiungila in
`HomePanel` con `tabs.addTab("Titolo", new MioPannello(...))`. Se deve aggiornarsi dopo ogni esame,
dagli un metodo `refresh()` e chiamalo da `HomePanel.refresh()`.

**Nuova schermata a tutta finestra** (come il report): aggiungila al `CardLayout` di `MainFrame` con un
nome (`root.add(pannello, "nome")`) e mostrala con `cards.show(root, "nome")`.

---

## 10. Verifiche prima di pubblicare una modifica

1. `./build.sh` (o `build.bat`) termina con "Fatto" e la validazione non mostra errori.
2. **Prova manuale** (5 minuti):
   - avvia il programma e controlla che non compaiano avvisi di caricamento;
   - Esame personalizzato → Studio → il dominio che hai modificato → "Mostra risposta" su qualche
     domanda nuova: testo, exhibit, opzioni, correzione e spiegazione devono essere corretti;
   - avvia e consegna un esame breve: il report e lo storico si aggiornano;
   - se hai toccato il cheat sheet, apri la pagina.
3. Fai il commit con un messaggio chiaro (`Aggiunge 10 domande su OSPF`, `Corregge spiegazione NA-013`).
4. Su GitHub la **CI** (`.github/workflows/build.yml`) ricompila e valida automaticamente a ogni push:
   se la spunta è rossa, apri il dettaglio e leggi l'errore.

---

## 11. Problemi comuni e soluzioni

| Sintomo | Causa probabile | Soluzione |
|---|---|---|
| Avviso "Alcune domande non sono state caricate" | errore di formato | leggi file e riga indicati; vedi §2 regole d'oro |
| La mia domanda non compare | file non in `index.txt` (strada B) o non `.txt` (strada A); ID duplicato | aggiungi a `index.txt`; usa un ID nuovo |
| Lettere accentate strane | file non salvato in UTF-8 | risalva in UTF-8 |
| Un pezzo di exhibit è finito in un altro campo | una riga dell'exhibit inizia con una parola chiave seguita da `:` | indenta quella riga |
| `javac` / `java` non trovato | JDK non installato o non nel PATH | reinstalla il JDK con l'opzione PATH, riapri il terminale |
| `error: invalid target release: 17` | JDK più vecchio di 17 | installa JDK 17+ |
| VS Code mostra errori ovunque | progetto non importato o JDK sbagliato | attendi "Java: Ready"; *Java: Configure Java Runtime*; *Java: Clean Java Language Server Workspace* |
| Lo storico non si salva | permessi sulla cartella utente | verifica i permessi di `~/.ccna-examlab` o usa `-Dccna.examlab.home` |
| Righe dello storico ignorate | file modificato a mano in modo errato | correggi o elimina le righe indicate nella console |
| Finestra troppo piccola / testo enorme | scaling dello schermo | modifica i font in `Ui.java` o prova `-Dsun.java2d.uiScale=1.25` |
| Domanda corretta segnata come sbagliata (FILL) | manca una forma della risposta | aggiungi l'alternativa con `\|` in `answer` |

---

## 12. Limiti noti e idee di miglioramento

**Limiti noti (consapevoli):**

- **Punteggio stimato**: proporzione lineare, non il metodo statistico di Cisco.
- **Arrotondamenti della selezione pesata**: se le quote arrotondate superano il totale, viene tolta
  l'ultima domanda aggiunta, che appartiene all'ultimo dominio in ordine (Automation). Effetto di ±1
  domanda; si potrebbe usare il "metodo dei resti maggiori".
- **Esami predefiniti** ricalcolati dalla banca: cambiano quando si aggiungono domande.
- **Niente salvataggio a metà esame**: chiudendo il programma l'esame in corso va perso.
- **Drag and drop** realizzato con menu a tendina invece del trascinamento.
- **Statistiche per ID**: rinominare l'ID di una domanda ne azzera la storia.
- **Banca domande** scritta senza revisione esterna: è possibile che contenga imprecisioni; segnalarle
  e correggerle è il miglior contributo possibile.
- **Nessun test automatico** oltre alla validazione delle domande.

**Idee di miglioramento, dalla più semplice:**

1. Correggere i riferimenti agli exam topic non allineati al v1.1 e completare gli argomenti mancanti
   (loop guard, BPDU filter, SOHO, gestione cloud).
2. Pulsante "Segnala/annota domanda" che salva una nota per ID.
3. Salvataggio e ripresa dell'esame in corso (serializzare `ExamSession` su file).
4. Esportazione del report in HTML/PDF.
5. Modalità flashcard per il cheat sheet (AD, porte, livelli syslog).
6. Test automatici: una classe `SelfTest` con `main` che verifica parser, correzione, mescolamento e
   generatore (senza librerie esterne), da lanciare nella CI.
7. Laboratori simulati (simlet) con una mini-CLI che interpreta un sottoinsieme di comandi `show`.
8. Supporto multi-blueprint (v1.1 / v2.0) selezionabile dall'utente (§6).
9. Tema scuro (tutti i colori sono già centralizzati in `Ui`).

---

## 13. Convenzioni del codice

- **Nessuna libreria esterna**: solo il JDK (`java.base`, `java.desktop`). Prima di aggiungere una
  dipendenza, chiedersi se è davvero indispensabile.
- **Dipendenze tra pacchetti**: `ui → engine → data → model`; `util` è usabile da tutti; `model` non
  dipende da nessun altro pacchetto del progetto. Mai far dipendere `model`, `data` o `engine` da `ui`.
- **Classi `final`** e campi `private final` quando possibile; oggetti immutabili per i dati condivisi.
- **Nomi** in inglese nel codice (classi in `PascalCase`, metodi e variabili in `camelCase`, costanti in
  `MAIUSCOLO_CON_UNDERSCORE`); **commenti, messaggi e interfaccia in italiano**.
- **Domande** in inglese (come l'esame), **spiegazioni** in italiano.
- **Errori non fatali** (un file, una riga, una domanda) vanno segnalati senza bloccare il programma.
- **Indentazione** di 4 spazi, parentesi graffe sempre presenti anche per `if` di una riga nel codice
  sorgente.
- Ogni classe ha un commento `/** ... */` che spiega a cosa serve.
- Quando modifichi una classe, aggiorna la sua scheda in `docs/0X-classi-*.md` e, se cambiano le righe,
  la sezione corrispondente in `docs/riga-per-riga/`.

---

## 14. Versioni e rilasci

1. Aggiorna la versione in **due** posti: `app/AppContext.java` (`VERSION`) e `pom.xml` (`<version>`).
   Schema consigliato *MAGGIORE.MINORE.CORREZIONE*: correzioni di domande → +0.0.1; nuove domande o
   funzioni → +0.1.0; cambi incompatibili (nuovo blueprint, formato dati) → +1.0.0.
2. `./build.sh` e prova manuale (§10).
3. Commit, push, e su GitHub crea una *Release* allegando `dist/ccna-examlab.jar` (la CI lo produce anche
   come artefatto scaricabile dal job "build").
