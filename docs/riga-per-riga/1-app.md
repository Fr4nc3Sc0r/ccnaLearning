# Riga per riga — pacchetto `app`

Il pacchetto `app` contiene il punto di partenza del programma (`Main`) e il contenitore dei servizi condivisi (`AppContext`). Per la descrizione generale vedi [07 - app e util](../07-classi-app-util.md).

**File in questo documento:** [`Main.java`](#mainjava) · [`AppContext.java`](#appcontextjava)

---

## Main.java

`src/main/java/com/ccnaexamlab/app/Main.java` — punto di ingresso: avvia la grafica o la validazione da riga di comando.

**Riga 1**

```java
1  package com.ccnaexamlab.app;
```

Dichiara il **pacchetto** del file: la classe vive nella "cartella logica" `com.ccnaexamlab.app`. Deve corrispondere alla cartella fisica `src/main/java/com/ccnaexamlab/app/`.

**Righe 3–9**

```java
3  import com.ccnaexamlab.data.QuestionBank;
4  import com.ccnaexamlab.data.UserDataStore;
5  import com.ccnaexamlab.ui.MainFrame;
6  
7  import javax.swing.JOptionPane;
8  import javax.swing.SwingUtilities;
9  import javax.swing.UIManager;
```

Gli `import` elencano le classi di altri pacchetti che useremo: la banca domande, il magazzino dei dati utente e la finestra principale (classi nostre), poi tre classi di Swing: `JOptionPane` (messaggi a comparsa), `SwingUtilities` (per lavorare sul filo della grafica) e `UIManager` (aspetto grafico).

**Riga 11**

```java
11  /** Punto di ingresso dell'applicazione. */
```

Commento di documentazione (Javadoc): spiega a cosa serve la classe. I commenti non vengono eseguiti.

**Riga 12**

```java
12  public final class Main {
```

Inizia la classe `Main`. `public` = visibile ovunque; `final` = nessuno può crearne una versione "derivata".

**Riga 14**

```java
14      private Main() { }
```

Costruttore **privato** e vuoto: impedisce di creare oggetti `Main`. La classe serve solo come contenitore di metodi statici.

**Riga 16**

```java
16      public static void main(String[] args) {
```

Il metodo `main` è la **porta d'ingresso**: è la prima cosa che la JVM esegue. `args` contiene le parole scritte dopo il comando (es. `--validate`).

**Righe 17–18**

```java
17          System.setProperty("awt.useSystemAAFontSettings", "on");
18          System.setProperty("swing.aatext", "true");
```

Due impostazioni di sistema che attivano l'**antialiasing** del testo (bordi delle lettere smussati, più leggibili), soprattutto su Linux.

**Righe 20–22**

```java
20          if (args.length > 0 && args[0].equals("--validate")) {
21              System.exit(validate());
22          }
```

Se è stato passato almeno un argomento e il primo è `--validate`, esegue la validazione delle domande e **termina** il programma con il codice restituito (0 = tutto ok, 1 = errori). `System.exit` chiude la JVM.

**Riga 24**

```java
24          SwingUtilities.invokeLater(() -> {
```

Tutto ciò che riguarda la grafica deve girare sul filo della grafica (EDT). `invokeLater` gli consegna il blocco di codice tra le graffe (una *lambda*) da eseguire appena possibile.

**Riga 25**

```java
25              setLookAndFeel();
```

Sceglie l'aspetto grafico (quello del sistema operativo, o quello richiesto con `-Dccna.laf`).

**Righe 26–27**

```java
26              UserDataStore store = new UserDataStore(UserDataStore.defaultDirectory());
27              store.load();
```

Crea il magazzino dei dati utente puntato su `~/.ccna-examlab` e lo carica: crea la cartella se manca, legge storico e statistiche.

**Riga 28**

```java
28              QuestionBank bank = QuestionBank.load(store.getUserQuestionsDirectory());
```

Carica tutte le domande: quelle incluse nel programma più quelle personali nella sottocartella `questions`.

**Riga 29**

```java
29              AppContext ctx = new AppContext(bank, store);
```

Mette insieme i servizi (banca, magazzino e, al suo interno, la fabbrica di esami) in un unico oggetto da passare alle finestre.

**Righe 30–31**

```java
30              MainFrame frame = new MainFrame(ctx);
31              frame.setVisible(true);
```

Crea la finestra principale e la rende visibile.

**Righe 32–37**

```java
32              if (!bank.getErrors().isEmpty()) {
33                  StringBuilder sb = new StringBuilder("Alcune domande non sono state caricate:\n\n");
34                  bank.getErrors().stream().limit(15).forEach(e -> sb.append("• ").append(e).append('\n'));
35                  JOptionPane.showMessageDialog(frame, sb.toString(), "Avvisi caricamento domande",
36                      JOptionPane.WARNING_MESSAGE);
37              }
```

Se durante il caricamento sono stati trovati errori nelle domande, prepara un messaggio con i primi 15 (`limit(15)`), ciascuno preceduto da un pallino, e lo mostra in una finestra di avviso sopra quella principale. Il programma continua comunque a funzionare con le domande valide.

**Righe 38–39**

```java
38          });
39      }
```

Fine della lambda passata a `invokeLater` e del metodo `main`.

**Riga 41**

```java
41      /** Modalità a riga di comando: verifica i file delle domande (utile prima di un commit). */
```

Metodo `validate`: usato da `--validate` e dagli script di build.

**Righe 42–48**

```java
42      private static int validate() {
43          QuestionBank bank = QuestionBank.load(null);
44          System.out.println("Domande caricate: " + bank.size());
45          bank.countByDomain().forEach((d, n) -> System.out.printf("  %-40s %3d%n", d.getLabel(), n));
46          bank.getErrors().forEach(e -> System.out.println("ERRORE: " + e));
47          return bank.getErrors().isEmpty() ? 0 : 1;
48      }
```

Carica solo le domande incluse (`null` = nessuna cartella utente), stampa il totale e, per ogni dominio, una riga formattata (`%-40s` = testo allineato a sinistra su 40 caratteri, `%3d` = numero su 3 cifre). Poi stampa ogni errore. Restituisce 0 se non ci sono errori, altrimenti 1 (operatore ternario `condizione ? a : b`).

**Righe 50–61**

```java
50      private static void setLookAndFeel() {
51          String laf = System.getProperty("ccna.laf");
52          try {
53              if (laf != null) {
54                  for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
55                      if (info.getName().equalsIgnoreCase(laf)) {
56                          UIManager.setLookAndFeel(info.getClassName());
57                          return;
58                      }
59                  }
60              }
61              UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
```

Legge la proprietà `ccna.laf` (passabile con `-Dccna.laf=Nimbus`). Se presente, cerca tra gli aspetti installati quello con lo stesso nome (ignorando maiuscole) e lo applica, poi esce con `return`. Altrimenti applica l'aspetto del sistema operativo.

**Righe 62–66**

```java
62          } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
63              System.err.println("Look and feel non disponibile: " + e.getMessage());
64          }
65      }
66  }
```

Se l'aspetto non si può caricare, invece di bloccarsi scrive un messaggio sull'uscita d'errore e prosegue con l'aspetto predefinito di Java.

---

## AppContext.java

`src/main/java/com/ccnaexamlab/app/AppContext.java` — contenitore dei servizi condivisi.

**Righe 1–5**

```java
1  package com.ccnaexamlab.app;
2  
3  import com.ccnaexamlab.data.QuestionBank;
4  import com.ccnaexamlab.data.UserDataStore;
5  import com.ccnaexamlab.engine.ExamFactory;
```

Pacchetto e import: usa la banca domande, il magazzino dati e la fabbrica di esami.

**Righe 7–8**

```java
7  /** Servizi condivisi dall'applicazione. */
8  public final class AppContext {
```

Inizio della classe `AppContext`.

**Righe 9–10**

```java
 9      public static final String APP_NAME = "CCNA ExamLab";
10      public static final String VERSION = "1.0.0";
```

Due **costanti** (`static final`): il nome e la versione del programma, mostrati nel titolo e nella finestra "Informazioni". La versione va tenuta allineata a quella in `pom.xml`.

**Righe 12–14**

```java
12      private final QuestionBank bank;
13      private final UserDataStore store;
14      private final ExamFactory factory;
```

I tre servizi. `private final`: visibili solo qui dentro e impostati una sola volta.

**Righe 16–20**

```java
16      public AppContext(QuestionBank bank, UserDataStore store) {
17          this.bank = bank;
18          this.store = store;
19          this.factory = new ExamFactory(bank, store);
20      }
```

Costruttore: memorizza banca e magazzino ricevuti (`this.bank` è il campo, `bank` il parametro) e crea la fabbrica di esami, che ha bisogno di entrambi.

**Righe 22–25**

```java
22      public QuestionBank getBank() { return bank; }
23      public UserDataStore getStore() { return store; }
24      public ExamFactory getFactory() { return factory; }
25  }
```

Tre **getter**: metodi che restituiscono i servizi a chi li chiede (le finestre).
