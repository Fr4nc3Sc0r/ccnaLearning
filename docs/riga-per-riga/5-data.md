# Riga per riga — pacchetto `data`

Il pacchetto `data` legge le domande dai file (`QuestionParser`, `QuestionBank`), ne genera di nuove (`SubnetQuestionGenerator`) e conserva i dati dell'utente (`UserDataStore`, `QuestionStat`). Descrizione generale: [04 - data](../04-classi-data.md).

**File in questo documento:** [`QuestionParseException.java`](#questionparseexceptionjava) · [`QuestionParser.java`](#questionparserjava) · [`QuestionBank.java`](#questionbankjava) · [`QuestionStat.java`](#questionstatjava) · [`UserDataStore.java`](#userdatastorejava) · [`SubnetQuestionGenerator.java`](#subnetquestiongeneratorjava)

---

## QuestionParseException.java

`src/main/java/com/ccnaexamlab/data/QuestionParseException.java` — errore di formato in un file di domande.

**Righe 1–3**

```java
1  package com.ccnaexamlab.data;
2  
3  /** Errore di formato in un file di domande. */
```

Pacchetto e commento.

**Righe 4–5**

```java
4  public class QuestionParseException extends Exception {
5      private static final long serialVersionUID = 1L;
```

Un nuovo tipo di errore: `extends Exception` significa "è un tipo di eccezione". `serialVersionUID` è un numero di versione richiesto da Java per gli oggetti serializzabili (le eccezioni lo sono); qui non ha effetti pratici.

**Righe 7–10**

```java
 7      public QuestionParseException(String message) {
 8          super(message);
 9      }
10  }
```

Il costruttore riceve il messaggio d'errore e lo passa alla classe madre con `super(...)`.

---

## QuestionParser.java

`src/main/java/com/ccnaexamlab/data/QuestionParser.java` — lettura del formato a blocchi `[Q]...[/Q]`.

**Righe 1–17**

```java
 1  package com.ccnaexamlab.data;
 2  
 3  import com.ccnaexamlab.model.Domain;
 4  import com.ccnaexamlab.model.Question;
 5  import com.ccnaexamlab.model.QuestionType;
 6  import com.ccnaexamlab.util.Text;
 7  
 8  import java.io.BufferedReader;
 9  import java.io.IOException;
10  import java.io.Reader;
11  import java.util.ArrayList;
12  import java.util.LinkedHashMap;
13  import java.util.List;
14  import java.util.Locale;
15  import java.util.Map;
16  import java.util.regex.Matcher;
17  import java.util.regex.Pattern;
```

Pacchetto e import: le classi del modello, `Text` per ripulire i blocchi e le classi Java per leggere testo ed espressioni regolari (`Pattern`, `Matcher`).

**Righe 19–43**

```java
19  /**
20   * Parser del formato testuale delle domande (vedi README, sezione "Formato delle domande").
21   *
22   * <pre>
23   * [Q]
24   * id: NF-001
25   * domain: 1
26   * type: SINGLE
27   * text:
28   * Testo della domanda...
29   * exhibit:
30   * R1#show ip route
31   * options:
32   * A) prima opzione
33   * B) seconda opzione
34   * answer: B
35   * explanation:
36   * Spiegazione...
37   * reference: Cisco IOS Configuration Guide
38   * [/Q]
39   * </pre>
40   *
41   * Le chiavi devono stare a inizio riga (colonna 0). Le righe che iniziano con '#'
42   * fuori da un blocco [Q] sono commenti.
43   */
```

Lungo commento con un esempio del formato. Il tag `<pre>` serve a mantenerlo impaginato nella documentazione Javadoc.

**Riga 44**

```java
44  public final class QuestionParser {
```

Inizio della classe.

**Righe 46–47**

```java
46      private static final Pattern KEY = Pattern.compile(
47          "^(id|domain|type|text|exhibit|options|answer|explanation|reference|items|targets|shuffle)\\s*:\\s?(.*)$");
```

Espressione regolare **precompilata** (per efficienza) che riconosce le righe "chiave: valore". Vedi la spiegazione nell'[algoritmo 1](../08-algoritmi.md#1-lettura-dei-file-delle-domande-parser).

**Riga 48**

```java
48      private static final Pattern OPTION = Pattern.compile("^\\s*([A-Za-z])[).]\\s+(.*)$");
```

Riconosce una riga di opzione: eventuali spazi, una lettera (gruppo 1), una parentesi chiusa o un punto, almeno uno spazio, il testo (gruppo 2).

**Riga 50**

```java
50      private QuestionParser() { }
```

Costruttore privato: la classe ha solo metodi statici.

**Righe 52–55**

```java
52      /**
53       * Legge tutte le domande dal reader. Le domande non valide vengono saltate e
54       * il relativo errore aggiunto a {@code errors}.
55       */
```

Commento del metodo `parse`.

**Riga 56**

```java
56      public static List<Question> parse(Reader reader, String source, List<String> errors) throws IOException {
```

Il metodo principale. Riceve una sorgente di testo, il nome del file (per i messaggi) e la lista in cui annotare gli errori. `throws IOException` avvisa che può fallire per un errore di lettura del disco.

**Righe 57–63**

```java
57          List<Question> result = new ArrayList<>();
58          BufferedReader br = new BufferedReader(reader);
59          String line;
60          int lineNo = 0;
61          int blockStart = 0;
62          Map<String, StringBuilder> fields = null;
63          String currentKey = null;
```

Variabili di lavoro: lista dei risultati, lettore a righe (`BufferedReader`), riga corrente, numero di riga, riga d'inizio del blocco, la "scheda" aperta (`fields`, una mappa chiave → testo; `null` se nessuna scheda è aperta) e la chiave che si sta riempiendo.

**Righe 65–67**

```java
65          while ((line = br.readLine()) != null) {
66              lineNo++;
67              String trimmed = line.trim();
```

Legge una riga alla volta finché il file non finisce (`readLine` restituisce `null` alla fine). Incrementa il numero di riga e prepara la versione senza spazi agli estremi.

**Righe 68–75**

```java
68              if (fields == null) {
69                  if (trimmed.equals("[Q]")) {
70                      fields = new LinkedHashMap<>();
71                      currentKey = null;
72                      blockStart = lineNo;
73                  }
74                  continue; // commenti e righe fuori blocco ignorati
75              }
```

Fuori da un blocco: se la riga è `[Q]` apre una scheda nuova (una `LinkedHashMap`, che ricorda l'ordine) e annota la riga di inizio; qualunque altra riga (commenti, righe vuote) viene ignorata con `continue` (passa alla riga successiva).

**Righe 76–84**

```java
76              if (trimmed.equals("[/Q]")) {
77                  try {
78                      result.add(build(fields, source));
79                  } catch (QuestionParseException | RuntimeException e) {
80                      errors.add(source + " (blocco alla riga " + blockStart + "): " + e.getMessage());
81                  }
82                  fields = null;
83                  continue;
84              }
```

Fine blocco: prova a costruire la domanda; se `build` segnala un problema (eccezione di formato o di coerenza), lo annota con file e riga d'inizio del blocco. In ogni caso chiude la scheda.

**Riga 85**

```java
85              Matcher m = KEY.matcher(line);
```

Applica l'espressione regolare alla riga **originale** (non ripulita: le chiavi devono essere a colonna 0).

**Righe 86–92**

```java
86              if (m.matches()) {
87                  currentKey = m.group(1);
88                  StringBuilder sb = new StringBuilder();
89                  if (!m.group(2).isBlank()) {
90                      sb.append(m.group(2));
91                  }
92                  fields.put(currentKey, sb);
```

Nuova chiave: diventa la chiave corrente; si crea un nuovo contenitore di testo con l'eventuale valore scritto sulla stessa riga e lo si mette nella scheda.

**Righe 93–100**

```java
 93              } else if (currentKey != null) {
 94                  StringBuilder sb = fields.get(currentKey);
 95                  if (sb.length() > 0) {
 96                      sb.append('\n');
 97                  }
 98                  sb.append(line);
 99              }
100          }
```

Riga di continuazione: viene aggiunta al testo della chiave corrente, preceduta da un a capo se il testo non è vuoto. Le righe prima della prima chiave vengono ignorate.

**Righe 101–105**

```java
101          if (fields != null) {
102              errors.add(source + ": blocco [Q] alla riga " + blockStart + " non chiuso con [/Q]");
103          }
104          return result;
105      }
```

Il file è finito con una scheda ancora aperta: errore "blocco non chiuso". Poi restituisce le domande valide.

**Righe 107–113**

```java
107      private static Question build(Map<String, StringBuilder> f, String source) throws QuestionParseException {
108          Question.Builder b = new Question.Builder().source(source);
109          String id = value(f, "id");
110          if (id.isEmpty()) {
111              throw new QuestionParseException("campo 'id' mancante");
112          }
113          b.id(id);
```

Trasforma la scheda in una domanda. Parte da un Builder con il nome del file; l'ID è obbligatorio.

**Righe 114–118**

```java
114          try {
115              b.domain(Domain.parse(value(f, "domain")));
116          } catch (IllegalArgumentException e) {
117              throw new QuestionParseException(id + ": " + e.getMessage());
118          }
```

Converte il testo del dominio con `Domain.parse`; se fallisce trasforma l'errore in un `QuestionParseException` con l'ID davanti.

**Righe 119–124**

```java
119          QuestionType type;
120          try {
121              type = QuestionType.valueOf(value(f, "type").toUpperCase(Locale.ROOT));
122          } catch (IllegalArgumentException e) {
123              throw new QuestionParseException(id + ": tipo non valido '" + value(f, "type") + "'");
124          }
```

Converte il tipo: `valueOf` cerca il valore dell'enum con quel nome esatto (per questo prima lo si mette in maiuscolo); un nome sconosciuto diventa un errore leggibile.

**Righe 125–129**

```java
125          b.type(type);
126          b.text(Text.trimBlock(raw(f, "text")));
127          b.exhibit(Text.trimBlock(raw(f, "exhibit")));
128          b.explanation(Text.trimBlock(raw(f, "explanation")));
129          b.reference(value(f, "reference"));
```

Copia tipo, testi (ripuliti con `trimBlock`, che conserva l'indentazione interna) e riferimento.

**Righe 130–131**

```java
130          String shuffle = value(f, "shuffle").toLowerCase(Locale.ROOT);
131          b.shuffleAllowed(!(shuffle.equals("no") || shuffle.equals("false")));
```

Il mescolamento è consentito salvo che il campo valga "no" o "false" (`!` = negazione).

**Righe 133–134**

```java
133          String answer = value(f, "answer");
134          switch (type) {
```

Legge la risposta e la interpreta secondo il tipo:

**Righe 135–148**

```java
135              case SINGLE:
136              case MULTI:
137                  parseOptions(raw(f, "options"), b);
138                  for (String a : answer.split("[,\\s]+")) {
139                      if (a.isEmpty()) {
140                          continue;
141                      }
142                      char c = Character.toUpperCase(a.charAt(0));
143                      if (a.length() != 1 || c < 'A' || c > 'Z') {
144                          throw new QuestionParseException(id + ": risposta non valida '" + a + "'");
145                      }
146                      b.correct(c - 'A');
147                  }
148                  break;
```

**SINGLE/MULTI**: prima legge le opzioni; poi divide `answer` su virgole e spazi (`[,\\s]+`), e per ogni pezzo verifica che sia una sola lettera A-Z e la converte in posizione (`c - 'A'`: 'C' − 'A' = 2).

**Righe 149–155**

```java
149              case FILL:
150                  for (String a : answer.split("\\|")) {
151                      if (!a.isBlank()) {
152                          b.accept(a.trim());
153                      }
154                  }
155                  break;
```

**FILL**: divide sulle barre verticali (`\\|`, protetta perché nelle regex `|` significa "oppure") e aggiunge ogni alternativa non vuota.

**Righe 156–166**

```java
156              case DRAG:
157                  for (String l : raw(f, "items").split("\n")) {
158                      if (l.isBlank()) {
159                          continue;
160                      }
161                      int idx = l.indexOf("=>");
162                      if (idx < 0) {
163                          throw new QuestionParseException(id + ": riga 'items' senza '=>': " + l);
164                      }
165                      b.dragPair(l.substring(0, idx).trim(), l.substring(idx + 2).trim());
166                  }
```

**DRAG**: per ogni riga non vuota di `items` cerca `=>`; ciò che sta prima è l'elemento, ciò che sta dopo la categoria giusta (`substring` estrae una parte di testo). Una riga senza `=>` è un errore.

**Righe 167–172**

```java
167                  for (String t : value(f, "targets").split("\\|")) {
168                      if (!t.isBlank()) {
169                          b.extraTarget(t.trim());
170                      }
171                  }
172                  break;
```

Aggiunge le eventuali categorie extra (distrattori).

**Righe 173–175**

```java
173              default:
174                  break;
175          }
```

Nessun altro tipo previsto.

**Righe 176–181**

```java
176          try {
177              return b.build();
178          } catch (IllegalStateException e) {
179              throw new QuestionParseException(e.getMessage());
180          }
181      }
```

Chiama i controlli di coerenza del Builder; se falliscono, il messaggio viene riportato come errore di formato.

**Righe 183–201**

```java
183      private static void parseOptions(String block, Question.Builder b) {
184          StringBuilder current = null;
185          List<String> opts = new ArrayList<>();
186          for (String l : block.split("\n")) {
187              Matcher m = OPTION.matcher(l);
188              if (m.matches()) {
189                  if (current != null) {
190                      opts.add(current.toString().trim());
191                  }
192                  current = new StringBuilder(m.group(2));
193              } else if (current != null && !l.isBlank()) {
194                  current.append(' ').append(l.trim());
195              }
196          }
197          if (current != null) {
198              opts.add(current.toString().trim());
199          }
200          opts.forEach(b::option);
201      }
```

Divide il blocco `options:` in opzioni. Per ogni riga: se inizia con "lettera)" chiude l'opzione precedente e ne apre una nuova; se è una riga non vuota senza lettera, la aggiunge all'opzione in corso (opzioni su più righe). Alla fine salva l'ultima opzione e le passa tutte al Builder (`b::option` = "chiama `b.option(...)` per ognuna"). Nota: la lettera scritta nel file non viene controllata, conta l'**ordine** delle righe.

**Righe 203–206**

```java
203      private static String raw(Map<String, StringBuilder> f, String key) {
204          StringBuilder sb = f.get(key);
205          return sb == null ? "" : sb.toString();
206      }
```

Testo di una chiave così com'è (o vuoto se la chiave manca).

**Righe 208–211**

```java
208      private static String value(Map<String, StringBuilder> f, String key) {
209          return raw(f, key).trim();
210      }
211  }
```

Come `raw`, ma senza spazi agli estremi: usato per i campi su una riga.

---

## QuestionBank.java

`src/main/java/com/ccnaexamlab/data/QuestionBank.java` — archivio di tutte le domande caricate.

**Righe 1–20**

```java
 1  package com.ccnaexamlab.data;
 2  
 3  import com.ccnaexamlab.model.Domain;
 4  import com.ccnaexamlab.model.Question;
 5  
 6  import java.io.BufferedReader;
 7  import java.io.IOException;
 8  import java.io.InputStream;
 9  import java.io.InputStreamReader;
10  import java.io.Reader;
11  import java.nio.charset.StandardCharsets;
12  import java.nio.file.DirectoryStream;
13  import java.nio.file.Files;
14  import java.nio.file.Path;
15  import java.util.ArrayList;
16  import java.util.Collections;
17  import java.util.EnumMap;
18  import java.util.LinkedHashMap;
19  import java.util.List;
20  import java.util.Map;
```

Pacchetto e import.

**Righe 22–26**

```java
22  /**
23   * Archivio delle domande. Carica le domande incluse nel programma (risorse elencate in
24   * {@code /questions/index.txt}) e quelle aggiunte dall'utente nella cartella
25   * {@code ~/.ccna-examlab/questions/*.txt}.
26   */
```

Commento della classe.

**Riga 27**

```java
27  public final class QuestionBank {
```

Inizio della classe.

**Riga 29**

```java
29      private static final String INDEX = "/questions/index.txt";
```

Percorso, dentro il `.jar`, del file che elenca i file di domande.

**Righe 31–32**

```java
31      private final Map<String, Question> byId = new LinkedHashMap<>();
32      private final List<String> errors = new ArrayList<>();
```

Le domande indicizzate per ID (mantenendo l'ordine di inserimento) e la lista degli errori di caricamento.

**Righe 34–41**

```java
34      public static QuestionBank load(Path userQuestionsDir) {
35          QuestionBank bank = new QuestionBank();
36          bank.loadBuiltIn();
37          if (userQuestionsDir != null && Files.isDirectory(userQuestionsDir)) {
38              bank.loadDirectory(userQuestionsDir);
39          }
40          return bank;
41      }
```

Crea una banca, carica le domande incluse e, se la cartella utente esiste ed è una cartella, anche quelle personali.

**Righe 43–48**

```java
43      private void loadBuiltIn() {
44          InputStream idx = QuestionBank.class.getResourceAsStream(INDEX);
45          if (idx == null) {
46              errors.add("Indice delle domande non trovato: " + INDEX);
47              return;
48          }
```

Apre l'indice come **risorsa** del programma (`getResourceAsStream`); se manca annota un errore e si ferma.

**Righe 49–65**

```java
49          try (BufferedReader br = new BufferedReader(new InputStreamReader(idx, StandardCharsets.UTF_8))) {
50              String line;
51              while ((line = br.readLine()) != null) {
52                  line = line.trim();
53                  if (line.isEmpty() || line.startsWith("#")) {
54                      continue;
55                  }
56                  String res = "/questions/" + line;
57                  InputStream in = QuestionBank.class.getResourceAsStream(res);
58                  if (in == null) {
59                      errors.add("Risorsa mancante: " + res);
60                      continue;
61                  }
62                  try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
63                      addAll(QuestionParser.parse(r, line, errors));
64                  }
65              }
```

`try (...)` "con risorse" chiude automaticamente il file alla fine. Per ogni riga dell'indice non vuota e non commentata costruisce il percorso della risorsa, la apre (o annota che manca) e la passa al parser; le domande ottenute vengono aggiunte alla banca.

**Righe 66–69**

```java
66          } catch (IOException e) {
67              errors.add("Errore di lettura delle domande: " + e.getMessage());
68          }
69      }
```

Errore di lettura: annotato, senza bloccare il programma.

**Righe 71–83**

```java
71      private void loadDirectory(Path dir) {
72          try (DirectoryStream<Path> ds = Files.newDirectoryStream(dir, "*.txt")) {
73              for (Path p : ds) {
74                  try (Reader r = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
75                      addAll(QuestionParser.parse(r, p.getFileName().toString(), errors));
76                  } catch (IOException e) {
77                      errors.add(p + ": " + e.getMessage());
78                  }
79              }
80          } catch (IOException e) {
81              errors.add(dir + ": " + e.getMessage());
82          }
83      }
```

Elenca i file `*.txt` della cartella utente (`newDirectoryStream` con un filtro) e li passa al parser uno a uno; un file illeggibile viene annotato e si passa al successivo.

**Righe 85–93**

```java
85      private void addAll(List<Question> list) {
86          for (Question q : list) {
87              if (byId.containsKey(q.getId())) {
88                  errors.add("ID duplicato ignorato: " + q.getId() + " (" + q.getSource() + ")");
89              } else {
90                  byId.put(q.getId(), q);
91              }
92          }
93      }
```

Aggiunge le domande alla mappa; se un ID esiste già, annota l'errore e ignora il duplicato (vince la prima domanda caricata).

**Riga 95**

```java
95      public List<Question> all() { return new ArrayList<>(byId.values()); }
```

Copia di tutte le domande (una copia, così chi la riceve può mescolarla senza alterare la banca).

**Righe 97–101**

```java
 97      public int size() { return byId.size(); }
 98  
 99      public Question get(String id) { return byId.get(id); }
100  
101      public List<String> getErrors() { return Collections.unmodifiableList(errors); }
```

Numero di domande, ricerca per ID e lista degli errori in sola lettura.

**Righe 103–111**

```java
103      public List<Question> byDomain(Domain d) {
104          List<Question> list = new ArrayList<>();
105          for (Question q : byId.values()) {
106              if (q.getDomain() == d) {
107                  list.add(q);
108              }
109          }
110          return list;
111      }
```

Nuova lista con le sole domande del dominio indicato.

**Righe 113–123**

```java
113      public Map<Domain, Integer> countByDomain() {
114          Map<Domain, Integer> m = new EnumMap<>(Domain.class);
115          for (Domain d : Domain.values()) {
116              m.put(d, 0);
117          }
118          for (Question q : byId.values()) {
119              m.merge(q.getDomain(), 1, Integer::sum);
120          }
121          return m;
122      }
123  }
```

Conteggio per dominio: prima mette 0 per tutti i domini (così compaiono anche quelli vuoti), poi per ogni domanda aggiunge 1 al suo dominio (`merge(..., 1, Integer::sum)` = "se c'è già somma 1").

---

## QuestionStat.java

`src/main/java/com/ccnaexamlab/data/QuestionStat.java` — statistiche persistenti di una domanda.

**Righe 1–3**

```java
1  package com.ccnaexamlab.data;
2  
3  /** Statistiche persistenti di una singola domanda. */
```

Pacchetto e commento.

**Righe 4–8**

```java
4  public final class QuestionStat {
5      private int seen;
6      private int correct;
7      private boolean lastCorrect;
8      private boolean flagged;
```

I quattro dati: viste, corrette, esito dell'ultima volta, marcata.

**Righe 10–17**

```java
10      public QuestionStat() { }
11  
12      QuestionStat(int seen, int correct, boolean lastCorrect, boolean flagged) {
13          this.seen = seen;
14          this.correct = correct;
15          this.lastCorrect = lastCorrect;
16          this.flagged = flagged;
17      }
```

Costruttore vuoto (tutto a zero/falso) e costruttore con valori, usato quando si rilegge `stats.tsv` (senza `public`: visibile solo nel pacchetto `data`).

**Righe 19–25**

```java
19      public void record(boolean ok) {
20          seen++;
21          if (ok) {
22              correct++;
23          }
24          lastCorrect = ok;
25      }
```

Registra un nuovo tentativo: +1 viste, +1 corrette se giusta, ricorda l'ultimo esito.

**Righe 27–33**

```java
27      public int getSeen() { return seen; }
28      public int getCorrect() { return correct; }
29      public int getWrong() { return seen - correct; }
30      public boolean isLastCorrect() { return lastCorrect; }
31      public boolean isFlagged() { return flagged; }
32      public void setFlagged(boolean flagged) { this.flagged = flagged; }
33  }
```

Getter; `getWrong` calcola le risposte sbagliate come differenza. Getter e setter della marcatura.

---

## UserDataStore.java

`src/main/java/com/ccnaexamlab/data/UserDataStore.java` — storico e statistiche su file.

**Righe 1–23**

```java
 1  package com.ccnaexamlab.data;
 2  
 3  import com.ccnaexamlab.model.Domain;
 4  import com.ccnaexamlab.model.DomainScore;
 5  import com.ccnaexamlab.model.ExamMode;
 6  import com.ccnaexamlab.model.ExamResult;
 7  import com.ccnaexamlab.model.ExamSession;
 8  import com.ccnaexamlab.model.Question;
 9  
10  import java.io.BufferedWriter;
11  import java.io.IOException;
12  import java.nio.charset.StandardCharsets;
13  import java.nio.file.Files;
14  import java.nio.file.Path;
15  import java.nio.file.Paths;
16  import java.nio.file.StandardOpenOption;
17  import java.time.LocalDateTime;
18  import java.util.ArrayList;
19  import java.util.Collections;
20  import java.util.EnumMap;
21  import java.util.HashMap;
22  import java.util.List;
23  import java.util.Map;
```

Pacchetto e import: modello, file (`java.nio.file`), date e collezioni.

**Righe 25–32**

```java
25  /**
26   * Dati persistenti dell'utente, salvati come file di testo (TSV) in {@code ~/.ccna-examlab}:
27   * <ul>
28   *   <li>{@code history.tsv} – storico degli esami svolti</li>
29   *   <li>{@code stats.tsv} – statistiche per domanda (viste, corrette, marcate)</li>
30   *   <li>{@code questions/} – cartella per domande aggiuntive dell'utente</li>
31   * </ul>
32   */
```

Commento con l'elenco dei file gestiti.

**Riga 33**

```java
33  public final class UserDataStore {
```

Inizio della classe.

**Righe 35–39**

```java
35      private final Path dir;
36      private final Path historyFile;
37      private final Path statsFile;
38      private final List<ExamResult> history = new ArrayList<>();
39      private final Map<String, QuestionStat> stats = new HashMap<>();
```

Percorsi della cartella e dei due file, lo storico in memoria e la mappa ID → statistiche.

**Righe 41–45**

```java
41      public UserDataStore(Path dir) {
42          this.dir = dir;
43          this.historyFile = dir.resolve("history.tsv");
44          this.statsFile = dir.resolve("stats.tsv");
45      }
```

Costruttore: calcola i percorsi dei file dentro la cartella (`resolve` = "cartella + nome file"). Non legge ancora nulla.

**Righe 47–53**

```java
47      public static Path defaultDirectory() {
48          String override = System.getProperty("ccna.examlab.home");
49          if (override != null && !override.isBlank()) {
50              return Paths.get(override);
51          }
52          return Paths.get(System.getProperty("user.home"), ".ccna-examlab");
53      }
```

Cartella predefinita: se è stata impostata la proprietà `ccna.examlab.home` usa quella, altrimenti `.ccna-examlab` nella cartella personale (`user.home`).

**Righe 55–57**

```java
55      public Path getDirectory() { return dir; }
56  
57      public Path getUserQuestionsDirectory() { return dir.resolve("questions"); }
```

Getter della cartella dati e della sottocartella delle domande personali.

**Righe 59–67**

```java
59      public void load() {
60          try {
61              Files.createDirectories(getUserQuestionsDirectory());
62          } catch (IOException e) {
63              System.err.println("Impossibile creare " + dir + ": " + e.getMessage());
64          }
65          loadHistory();
66          loadStats();
67      }
```

Crea (se mancano) la cartella e la sottocartella `questions` — `createDirectories` crea anche le cartelle intermedie — poi carica storico e statistiche. Un errore di creazione viene solo segnalato.

**Riga 69**

```java
69      // ------------------------------------------------------------------ storico
```

Separatore visivo: inizia la parte dedicata allo storico.

**Riga 71**

```java
71      public List<ExamResult> getHistory() { return Collections.unmodifiableList(history); }
```

Lo storico in sola lettura.

**Righe 73–92**

```java
73      private void loadHistory() {
74          history.clear();
75          if (!Files.exists(historyFile)) {
76              return;
77          }
78          try {
79              for (String line : Files.readAllLines(historyFile, StandardCharsets.UTF_8)) {
80                  if (line.isBlank() || line.startsWith("#")) {
81                      continue;
82                  }
83                  try {
84                      history.add(parseHistoryLine(line));
85                  } catch (RuntimeException e) {
86                      System.err.println("Riga di storico ignorata: " + line);
87                  }
88              }
89          } catch (IOException e) {
90              System.err.println("Errore lettura storico: " + e.getMessage());
91          }
92      }
```

Svuota lo storico in memoria; se il file non esiste ha finito. Altrimenti legge tutte le righe (UTF-8), salta quelle vuote o di commento e prova a convertire ognuna: una riga rovinata viene ignorata con un messaggio, senza perdere le altre.

**Righe 94–111**

```java
 94      private static ExamResult parseHistoryLine(String line) {
 95          String[] p = line.split("\t", -1);
 96          LocalDateTime date = LocalDateTime.parse(p[0]);
 97          String title = p[1];
 98          ExamMode mode = ExamMode.valueOf(p[2]);
 99          int correct = Integer.parseInt(p[3]);
100          int total = Integer.parseInt(p[4]);
101          int duration = Integer.parseInt(p[5]);
102          Map<Domain, DomainScore> byDomain = new EnumMap<>(Domain.class);
103          if (p.length > 6 && !p[6].isEmpty()) {
104              for (String part : p[6].split(";")) {
105                  String[] kv = part.split("[:/]");
106                  byDomain.put(Domain.valueOf(kv[0]),
107                      new DomainScore(Integer.parseInt(kv[1]), Integer.parseInt(kv[2])));
108              }
109          }
110          return new ExamResult(title, mode, date, correct, total, duration, byDomain, null);
111      }
```

Converte una riga in risultato: divide sui TAB (`-1` mantiene anche i campi vuoti finali), poi converte data, titolo, modalità e numeri. Il settimo campo (`p[6]`), se presente, contiene `DOMINIO:corrette/totale` separati da `;`: lo divide e ricostruisce i `DomainScore`. La sessione è `null` (non viene salvata).

**Righe 113–130**

```java
113      private static String toHistoryLine(ExamResult r) {
114          StringBuilder d = new StringBuilder();
115          for (Map.Entry<Domain, DomainScore> e : r.getByDomain().entrySet()) {
116              if (d.length() > 0) {
117                  d.append(';');
118              }
119              d.append(e.getKey().name()).append(':').append(e.getValue().getCorrect())
120               .append('/').append(e.getValue().getTotal());
121          }
122          return String.join("\t",
123              r.getDate().withNano(0).toString(),
124              r.getTitle().replace('\t', ' '),
125              r.getMode().name(),
126              String.valueOf(r.getCorrect()),
127              String.valueOf(r.getTotal()),
128              String.valueOf(r.getDurationSeconds()),
129              d.toString());
130      }
```

Operazione inversa: costruisce il dettaglio per dominio (`;` tra i domini) e unisce i campi con TAB. La data viene salvata senza frazioni di secondo (`withNano(0)`); eventuali TAB nel titolo vengono sostituiti da spazi per non rompere il formato.

**Riga 132**

```java
132      /** Registra il risultato nello storico e aggiorna le statistiche delle domande. */
```

`recordResult` ([algoritmo 8](../08-algoritmi.md#8-salvataggio-del-risultato)).

**Righe 133–157**

```java
133      public void recordResult(ExamResult result) {
134          history.add(result);
135          try {
136              Files.createDirectories(dir);
137              Files.writeString(historyFile, toHistoryLine(result) + System.lineSeparator(),
138                  StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
139          } catch (IOException e) {
140              System.err.println("Errore salvataggio storico: " + e.getMessage());
141          }
142          ExamSession s = result.getSession();
143          if (s != null) {
144              for (int i = 0; i < s.size(); i++) {
145                  Question q = s.getQuestion(i);
146                  if (q.isGenerated()) {
147                      continue;
148                  }
149                  QuestionStat st = stat(q.getId());
150                  st.record(q.isCorrect(s.getResponse(i)));
151                  if (s.isMarked(i)) {
152                      st.setFlagged(true);
153                  }
154              }
155              saveStats();
156          }
157      }
```

Aggiunge il risultato in memoria e in coda al file (`CREATE` + `APPEND`); poi, se c'è la sessione, aggiorna la statistica di ogni domanda non generata (esito e marcatura) e salva le statistiche.

**Righe 159–168**

```java
159      public void clearHistory() {
160          history.clear();
161          stats.clear();
162          try {
163              Files.deleteIfExists(historyFile);
164              Files.deleteIfExists(statsFile);
165          } catch (IOException e) {
166              System.err.println("Errore cancellazione dati: " + e.getMessage());
167          }
168      }
```

Svuota storico e statistiche in memoria e cancella i due file (`deleteIfExists` non dà errore se il file non c'è).

**Riga 170**

```java
170      // ---------------------------------------------------------- statistiche
```

Separatore: inizia la parte dedicata alle statistiche.

**Riga 172**

```java
172      public QuestionStat stat(String id) { return stats.computeIfAbsent(id, k -> new QuestionStat()); }
```

Restituisce la statistica dell'ID, creandola vuota se non esiste.

**Riga 174**

```java
174      public QuestionStat peekStat(String id) { return stats.get(id); }
```

Restituisce la statistica o `null`, senza crearla (lettura pura).

**Righe 176–179**

```java
176      public void setFlagged(String id, boolean flagged) {
177          stat(id).setFlagged(flagged);
178          saveStats();
179      }
```

Imposta la marcatura e salva subito.

**Righe 181–202**

```java
181      private void loadStats() {
182          stats.clear();
183          if (!Files.exists(statsFile)) {
184              return;
185          }
186          try {
187              for (String line : Files.readAllLines(statsFile, StandardCharsets.UTF_8)) {
188                  String[] p = line.split("\t");
189                  if (p.length < 5 || line.startsWith("#")) {
190                      continue;
191                  }
192                  try {
193                      stats.put(p[0], new QuestionStat(Integer.parseInt(p[1]), Integer.parseInt(p[2]),
194                          Boolean.parseBoolean(p[3]), Boolean.parseBoolean(p[4])));
195                  } catch (NumberFormatException e) {
196                      System.err.println("Riga di statistiche ignorata: " + line);
197                  }
198              }
199          } catch (IOException e) {
200              System.err.println("Errore lettura statistiche: " + e.getMessage());
201          }
202      }
```

Legge `stats.tsv`: per ogni riga con almeno 5 campi e che non è un commento crea una `QuestionStat`; le righe con numeri non validi vengono ignorate con un messaggio.

**Righe 204–221**

```java
204      private void saveStats() {
205          try {
206              Files.createDirectories(dir);
207              try (BufferedWriter w = Files.newBufferedWriter(statsFile, StandardCharsets.UTF_8)) {
208                  w.write("# id\tseen\tcorrect\tlastCorrect\tflagged");
209                  w.newLine();
210                  for (Map.Entry<String, QuestionStat> e : stats.entrySet()) {
211                      QuestionStat s = e.getValue();
212                      w.write(e.getKey() + "\t" + s.getSeen() + "\t" + s.getCorrect() + "\t"
213                          + s.isLastCorrect() + "\t" + s.isFlagged());
214                      w.newLine();
215                  }
216              }
217          } catch (IOException e) {
218              System.err.println("Errore salvataggio statistiche: " + e.getMessage());
219          }
220      }
221  }
```

Riscrive l'intero file: intestazione di commento, poi una riga per ogni statistica con i campi separati da TAB. Il `try` interno con risorse chiude il file anche in caso di errore.

---

## SubnetQuestionGenerator.java

`src/main/java/com/ccnaexamlab/data/SubnetQuestionGenerator.java` — generatore di esercizi di subnetting.

**Righe 1–13**

```java
 1  package com.ccnaexamlab.data;
 2  
 3  import com.ccnaexamlab.model.Domain;
 4  import com.ccnaexamlab.model.Question;
 5  import com.ccnaexamlab.model.QuestionType;
 6  import com.ccnaexamlab.util.IpUtils;
 7  
 8  import java.util.ArrayList;
 9  import java.util.Collections;
10  import java.util.LinkedHashSet;
11  import java.util.List;
12  import java.util.Random;
13  import java.util.Set;
```

Pacchetto e import.

**Righe 15–18**

```java
15  /**
16   * Genera domande di subnetting IPv4 sempre nuove, con la spiegazione passo-passo
17   * del metodo del "magic number" (block size).
18   */
```

Commento della classe.

**Riga 19**

```java
19  public final class SubnetQuestionGenerator {
```

Inizio della classe.

**Riga 21**

```java
21      private static final String REF = "CCNA 200-301 exam topic 1.6 – Configure and verify IPv4 addressing and subnetting";
```

Riferimento comune a tutte le domande generate.

**Righe 23–24**

```java
23      private final Random random;
24      private int counter;
```

Il generatore casuale (ricevuto dall'esterno, così nei test si può usare un seme fisso) e il contatore delle domande prodotte.

**Righe 26–28**

```java
26      public SubnetQuestionGenerator(Random random) {
27          this.random = random;
28      }
```

Costruttore.

**Righe 30–36**

```java
30      public List<Question> generate(int count) {
31          List<Question> list = new ArrayList<>();
32          for (int i = 0; i < count; i++) {
33              list.add(generateOne());
34          }
35          return list;
36      }
```

Genera `count` domande chiamando ripetutamente `generateOne`.

**Righe 38–49**

```java
38      public Question generateOne() {
39          counter++;
40          int variant = random.nextInt(6);
41          switch (variant) {
42              case 0: return networkAddress();
43              case 1: return broadcastAddress();
44              case 2: return lastUsable();
45              case 3: return hostsPerSubnet();
46              case 4: return maskForHosts();
47              default: return validHost();
48          }
49      }
```

Incrementa il contatore, sceglie una variante tra 0 e 5 e chiama il metodo corrispondente (`default` copre il 5).

**Riga 51**

```java
51      // ------------------------------------------------------------------ helpers
```

Separatore: metodi di supporto.

**Riga 53**

```java
53      private int randomPrefix(int min, int max) { return min + random.nextInt(max - min + 1); }
```

Prefisso casuale tra `min` e `max` inclusi (`nextInt(n)` restituisce un numero da 0 a n−1).

**Righe 55–73**

```java
55      private long randomIp(int prefix) {
56          long base;
57          if (prefix >= 24) {
58              switch (random.nextInt(3)) {
59                  case 0: base = IpUtils.toLong("10.0.0.0") | ((long) random.nextInt(1 << 24)); break;
60                  case 1: base = IpUtils.toLong("172.16.0.0") | ((long) random.nextInt(1 << 20)); break;
61                  default: base = IpUtils.toLong("192.168.0.0") | ((long) random.nextInt(1 << 16)); break;
62              }
63          } else if (random.nextBoolean()) {
64              base = IpUtils.toLong("10.0.0.0") | ((long) random.nextInt(1 << 24));
65          } else {
66              base = IpUtils.toLong("172.16.0.0") | ((long) random.nextInt(1 << 20));
67          }
68          long net = IpUtils.network(base, prefix);
69          long bc = IpUtils.broadcast(base, prefix);
70          // un host valido (né rete né broadcast)
71          long span = bc - net - 1;
72          return net + 1 + (long) (random.nextDouble() * span);
73      }
```

Indirizzo casuale: per prefissi ≥ /24 sceglie a caso tra le tre reti private, altrimenti tra 10.x e 172.16-31.x (192.168/16 è troppo piccola per prefissi corti). Alla base aggiunge un numero casuale nei bit disponibili (`1 << 24` = 2^24). Poi calcola rete e broadcast e sceglie un punto a caso **strettamente** tra i due, così l'indirizzo è sempre un host valido.

**Righe 75–83**

```java
75      private Question.Builder base(QuestionType type) {
76          return new Question.Builder()
77              .id(String.format("GEN-SUB-%04d", counter))
78              .domain(Domain.NETWORK_FUNDAMENTALS)
79              .type(type)
80              .generated(true)
81              .source("generatore")
82              .reference(REF);
83      }
```

Builder precompilato: ID `GEN-SUB-0001` (`%04d` = numero su 4 cifre con zeri), dominio 1.0, tipo, flag "generata", sorgente e riferimento.

**Riga 85**

```java
85      /** Spiegazione col metodo del magic number. */
```

`method`: costruisce la spiegazione in 5 passi ([algoritmo 7](../08-algoritmi.md#7-generatore-di-subnetting-e-metodo-del-magic-number)).

**Righe 86–94**

```java
86      private static String method(long ip, int prefix) {
87          int octetIndex = (prefix - 1) / 8;           // 0..3: ottetto "interessante"
88          long mask = IpUtils.mask(prefix);
89          int maskOctet = (int) ((mask >> (24 - 8 * octetIndex)) & 255);
90          int block = 256 - maskOctet;
91          int ipOctet = (int) ((ip >> (24 - 8 * octetIndex)) & 255);
92          int netOctet = (ipOctet / block) * block;
93          long net = IpUtils.network(ip, prefix);
94          long bc = IpUtils.broadcast(ip, prefix);
```

Calcola l'ottetto interessante, il valore della maschera in quell'ottetto, il block size, il valore dell'host in quell'ottetto e l'inizio del suo blocco (divisione intera e moltiplicazione), poi rete e broadcast completi.

**Righe 95–108**

```java
 95          StringBuilder sb = new StringBuilder();
 96          sb.append("Metodo del \"magic number\":\n");
 97          sb.append("1) /").append(prefix).append(" = maschera ").append(IpUtils.maskString(prefix)).append(".\n");
 98          sb.append("2) L'ottetto interessante è il ").append(octetIndex + 1)
 99            .append("° (valore di maschera ").append(maskOctet).append(").\n");
100          sb.append("3) Block size = 256 - ").append(maskOctet).append(" = ").append(block)
101            .append(": le subnet partono da multipli di ").append(block).append(".\n");
102          sb.append("4) Nel ").append(octetIndex + 1).append("° ottetto l'host vale ").append(ipOctet)
103            .append(", che cade nel blocco ").append(netOctet).append("–").append(netOctet + block - 1).append(".\n");
104          sb.append("5) Rete: `").append(IpUtils.toIp(net)).append("`  Broadcast: `").append(IpUtils.toIp(bc))
105            .append("`\n   Host utilizzabili: `").append(IpUtils.toIp(net + 1)).append("` – `")
106            .append(IpUtils.toIp(bc - 1)).append("` (").append(IpUtils.usableHosts(prefix)).append(" host).");
107          return sb.toString();
108      }
```

Scrive i cinque passi uno per riga, con i valori calcolati; gli indirizzi sono racchiusi tra apici inversi per evidenziarli.

**Riga 110**

```java
110      // ------------------------------------------------------------------ varianti
```

Separatore: le sei varianti di domanda.

**Righe 112–122**

```java
112      private Question networkAddress() {
113          int p = randomPrefix(17, 30);
114          long ip = randomIp(p);
115          String net = IpUtils.toIp(IpUtils.network(ip, p));
116          return base(QuestionType.FILL)
117              .text("What is the subnet (network) address of host " + IpUtils.toIp(ip) + "/" + p + "?\n"
118                  + "Enter the address in dotted-decimal format.")
119              .accept(net).accept(net + "/" + p)
120              .explanation(method(ip, p))
121              .build();
122      }
```

Variante "indirizzo di rete": prefisso /17-/30, indirizzo casuale, risposta accettata sia come "rete" sia come "rete/prefisso".

**Righe 124–134**

```java
124      private Question broadcastAddress() {
125          int p = randomPrefix(17, 30);
126          long ip = randomIp(p);
127          String bc = IpUtils.toIp(IpUtils.broadcast(ip, p));
128          return base(QuestionType.FILL)
129              .text("A host is configured with IP address " + IpUtils.toIp(ip) + " and subnet mask "
130                  + IpUtils.maskString(p) + ". What is the broadcast address of its subnet?")
131              .accept(bc)
132              .explanation(method(ip, p) + "\n\nTrucco: il broadcast è sempre (indirizzo della subnet successiva) - 1.")
133              .build();
134      }
```

Variante "broadcast": la domanda fornisce IP e maschera puntata; spiegazione con il trucco "subnet successiva − 1".

**Righe 136–146**

```java
136      private Question lastUsable() {
137          int p = randomPrefix(20, 30);
138          long ip = randomIp(p);
139          String last = IpUtils.toIp(IpUtils.broadcast(ip, p) - 1);
140          return base(QuestionType.FILL)
141              .text("What is the last usable host address in the subnet that contains " + IpUtils.toIp(ip)
142                  + "/" + p + "?")
143              .accept(last)
144              .explanation(method(ip, p) + "\n\nL'ultimo host utilizzabile è il broadcast meno 1.")
145              .build();
146      }
```

Variante "ultimo host": prefisso /20-/30; la risposta è broadcast − 1.

**Righe 148–162**

```java
148      private Question hostsPerSubnet() {
149          int p = randomPrefix(18, 30);
150          long hosts = IpUtils.usableHosts(p);
151          int hostBits = 32 - p;
152          boolean useMask = random.nextBoolean();
153          String spec = useMask ? "subnet mask " + IpUtils.maskString(p) : "a /" + p + " prefix";
154          return base(QuestionType.FILL)
155              .text("How many usable host addresses are available in a subnet with " + spec + "?")
156              .accept(String.valueOf(hosts))
157              .explanation("Bit di host = 32 - " + p + " = " + hostBits + ".\n"
158                  + "Host utilizzabili = 2^" + hostBits + " - 2 = " + (1L << hostBits) + " - 2 = " + hosts + ".\n\n"
159                  + "Si sottraggono 2 indirizzi: quello di rete (tutti i bit di host a 0) e il broadcast "
160                  + "(tutti i bit di host a 1). Eccezione: /31 sui link punto-punto (RFC 3021) usa entrambi gli indirizzi.")
161              .build();
162      }
```

Variante "quanti host": prefisso /18-/30; a caso la domanda usa la maschera puntata o il prefisso. La spiegazione mostra il calcolo 2^n − 2 e l'eccezione /31.

**Righe 164–174**

```java
164      private Question maskForHosts() {
165          int targetPrefix = randomPrefix(20, 29);
166          long max = IpUtils.usableHosts(targetPrefix);
167          long min = IpUtils.usableHosts(targetPrefix + 1) + 1;
168          long needed = min + (long) (random.nextDouble() * (max - min + 1));
169          Set<Integer> prefixes = new LinkedHashSet<>();
170          prefixes.add(targetPrefix);
171          prefixes.add(targetPrefix + 1);
172          prefixes.add(targetPrefix - 1);
173          prefixes.add(targetPrefix + 2 <= 30 ? targetPrefix + 2 : targetPrefix - 2);
174          List<Integer> opts = new ArrayList<>(prefixes);
```

Variante "maschera per N host": sceglie il prefisso giusto, calcola l'intervallo di host per cui è il più adatto (più del prefisso successivo, al massimo il suo) e prende un numero di host a caso in quell'intervallo. Le opzioni sono il prefisso giusto, quello successivo, quello precedente e un quarto (2 più grande o 2 più piccolo), raccolte in un `LinkedHashSet` per evitare doppioni.

**Righe 175–186**

```java
175          Collections.shuffle(opts, random);
176          Question.Builder b = base(QuestionType.SINGLE)
177              .text("A network engineer must create a subnet that supports " + needed + " hosts and wastes "
178                  + "the fewest addresses. Which subnet mask should be used?");
179          for (int i = 0; i < opts.size(); i++) {
180              int op = opts.get(i);
181              b.option(IpUtils.maskString(op) + " (/" + op + ")");
182              if (op == targetPrefix) {
183                  b.correct(i);
184              }
185          }
186          int hb = 32 - targetPrefix;
```

Mescola le opzioni, scrive il testo e aggiunge ogni opzione come "maschera (/prefisso)", segnando corretta quella giusta.

**Righe 187–192**

```java
187          return b.explanation("Serve il più piccolo n tale che 2^n - 2 ≥ " + needed + ".\n"
188                  + "2^" + (hb - 1) + " - 2 = " + IpUtils.usableHosts(targetPrefix + 1) + " (insufficiente)\n"
189                  + "2^" + hb + " - 2 = " + max + " (sufficiente)\n"
190                  + "Quindi " + hb + " bit di host → /" + targetPrefix + " = " + IpUtils.maskString(targetPrefix) + ".")
191              .build();
192      }
```

Spiegazione: confronta i due prefissi vicini e motiva la scelta.

**Righe 194–204**

```java
194      private Question validHost() {
195          int p = randomPrefix(24, 29);
196          long ip = randomIp(p);
197          long net = IpUtils.network(ip, p);
198          long bc = IpUtils.broadcast(ip, p);
199          long size = bc - net + 1;
200          List<long[]> candidates = new ArrayList<>(); // {address, isValid}
201          candidates.add(new long[] {ip, 1});
202          candidates.add(new long[] {net, 0});
203          candidates.add(new long[] {bc, 0});
204          candidates.add(new long[] {bc + 1 + random.nextInt((int) Math.max(1, size - 2)), 0}); // subnet successiva
```

Variante "host valido": prefisso /24-/29; prepara quattro candidati come coppie {indirizzo, valido?}: l'host, l'indirizzo di rete, il broadcast e un indirizzo della subnet successiva.

**Righe 205–218**

```java
205          Collections.shuffle(candidates, random);
206          Question.Builder b = base(QuestionType.SINGLE)
207              .text("Which address is a valid host address in subnet " + IpUtils.toIp(net) + "/" + p + "?");
208          for (int i = 0; i < candidates.size(); i++) {
209              b.option(IpUtils.toIp(candidates.get(i)[0]));
210              if (candidates.get(i)[1] == 1) {
211                  b.correct(i);
212              }
213          }
214          return b.explanation(method(ip, p)
215                  + "\n\nLe opzioni errate sono l'indirizzo di rete, il broadcast oppure un host di un'altra subnet.")
216              .build();
217      }
218  }
```

Mescola i candidati, li aggiunge come opzioni e segna corretto quello valido; la spiegazione aggiunge perché gli altri sono sbagliati.
