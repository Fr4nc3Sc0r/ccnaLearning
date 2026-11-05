# Riga per riga — pacchetto `model`

Il pacchetto `model` definisce i concetti del programma: dominio, tipo di domanda, modalità, domanda, risposta, esame in corso, punteggi e risultato. Descrizione generale: [03 - model](../03-classi-model.md).

**File in questo documento:** [`Domain.java`](#domainjava) · [`QuestionType.java`](#questiontypejava) · [`ExamMode.java`](#exammodejava) · [`Response.java`](#responsejava) · [`DomainScore.java`](#domainscorejava) · [`Question.java`](#questionjava) · [`ExamSession.java`](#examsessionjava) · [`ExamResult.java`](#examresultjava)

---

## Domain.java

`src/main/java/com/ccnaexamlab/model/Domain.java` — i sei domini del blueprint con codice, nome e peso.

**Riga 1**

```java
1  package com.ccnaexamlab.model;
```

Pacchetto `model`.

**Righe 3–6**

```java
3  /**
4   * I sei domini (exam topics) del blueprint Cisco CCNA 200-301 v1.1
5   * con il relativo peso percentuale all'esame.
6   */
```

Commento che descrive l'enum.

**Riga 7**

```java
7  public enum Domain {
```

Un **enum** è un tipo con un elenco chiuso di valori. Qui i valori sono i sei domini.

**Righe 8–13**

```java
 8      NETWORK_FUNDAMENTALS("1.0", "Network Fundamentals", 20),
 9      NETWORK_ACCESS("2.0", "Network Access", 20),
10      IP_CONNECTIVITY("3.0", "IP Connectivity", 25),
11      IP_SERVICES("4.0", "IP Services", 10),
12      SECURITY_FUNDAMENTALS("5.0", "Security Fundamentals", 15),
13      AUTOMATION("6.0", "Automation and Programmability", 10);
```

I sei valori. Tra parentesi i tre dati di ciascuno (codice, nome, peso percentuale), passati al costruttore delle righe 19-23. Il punto e virgola dopo l'ultimo valore chiude l'elenco.

**Righe 15–17**

```java
15      private final String code;
16      private final String title;
17      private final int weight;
```

I campi in cui ogni valore ricorda i propri tre dati.

**Righe 19–23**

```java
19      Domain(String code, String title, int weight) {
20          this.code = code;
21          this.title = title;
22          this.weight = weight;
23      }
```

Costruttore dell'enum: viene chiamato automaticamente una volta per ciascun valore e salva i dati nei campi.

**Righe 25–28**

```java
25      public String getCode() { return code; }
26      public String getTitle() { return title; }
27      /** Peso percentuale del dominio all'esame reale. */
28      public int getWeight() { return weight; }
```

Getter: restituiscono codice, nome e peso.

**Riga 30**

```java
30      public String getLabel() { return code + " " + title; }
```

Etichetta leggibile, es. "3.0 IP Connectivity" (il `+` unisce i testi).

**Riga 32**

```java
32      /** Accetta il nome dell'enum (NETWORK_ACCESS), il numero (2) o il codice (2.0). */
```

Metodo `parse`: converte il testo scritto nel file delle domande in un dominio.

**Righe 33–39**

```java
33      public static Domain parse(String s) {
34          String v = s.trim();
35          for (Domain d : values()) {
36              if (d.name().equalsIgnoreCase(v) || d.code.equals(v) || d.code.startsWith(v + ".")) {
37                  return d;
38              }
39          }
```

`trim()` toglie gli spazi; poi per ogni dominio (`values()` restituisce tutti i valori) controlla se il testo coincide con il nome (ignorando maiuscole), con il codice ("2.0") o se il codice inizia con il testo seguito da un punto ("2" → "2."). Al primo che corrisponde lo restituisce.

**Righe 40–41**

```java
40          throw new IllegalArgumentException("Dominio sconosciuto: " + s);
41      }
```

Se nessun dominio corrisponde **lancia un'eccezione** (segnala l'errore): il parser la intercetta e la trasforma in un messaggio.

**Righe 43–45**

```java
43      @Override
44      public String toString() { return getLabel(); }
45  }
```

`toString` è il metodo usato da Java quando un oggetto deve diventare testo (es. in un menu a tendina): qui restituisce l'etichetta. `@Override` indica che ridefinisce un metodo ereditato.

---

## QuestionType.java

`src/main/java/com/ccnaexamlab/model/QuestionType.java` — i quattro tipi di domanda.

**Righe 1–3**

```java
1  package com.ccnaexamlab.model;
2  
3  /** Tipologie di domanda supportate (come all'esame reale). */
```

Pacchetto e commento della classe.

**Righe 4–12**

```java
 4  public enum QuestionType {
 5      /** Scelta multipla con una sola risposta corretta. */
 6      SINGLE("Risposta singola"),
 7      /** Scelta multipla con più risposte corrette ("Choose two"). */
 8      MULTI("Risposta multipla"),
 9      /** Abbinamento elementi → categorie (equivalente del drag and drop). */
10      DRAG("Drag and drop"),
11      /** Risposta libera (es. calcolo di subnetting). */
12      FILL("Risposta aperta");
```

Enum con i quattro tipi; ognuno ha un commento e un'etichetta in italiano tra parentesi.

**Righe 14–19**

```java
14      private final String label;
15  
16      QuestionType(String label) { this.label = label; }
17  
18      public String getLabel() { return label; }
19  }
```

Il campo con l'etichetta, il costruttore che la salva e il getter che la restituisce.

---

## ExamMode.java

`src/main/java/com/ccnaexamlab/model/ExamMode.java` — le due modalità d'esame.

**Righe 1–3**

```java
1  package com.ccnaexamlab.model;
2  
3  /** Modalità d'esame. */
```

Pacchetto e commento.

**Righe 4–8**

```java
4  public enum ExamMode {
5      /** Condizioni d'esame reale: tempo limite, nessuna spiegazione fino alla fine. */
6      SIMULATION("Simulazione"),
7      /** Senza limite di tempo, con risposta e spiegazione visibili su richiesta. */
8      STUDY("Studio");
```

I due valori, Simulazione e Studio, ciascuno con la sua etichetta.

**Righe 10–18**

```java
10      private final String label;
11  
12      ExamMode(String label) { this.label = label; }
13  
14      public String getLabel() { return label; }
15  
16      @Override
17      public String toString() { return label; }
18  }
```

Campo, costruttore e getter dell'etichetta; `toString` restituisce l'etichetta così i menu a tendina mostrano "Studio" invece di "STUDY".

---

## Response.java

`src/main/java/com/ccnaexamlab/model/Response.java` — la risposta dell'utente a una domanda.

**Righe 1–7**

```java
1  package com.ccnaexamlab.model;
2  
3  import java.util.Collections;
4  import java.util.HashMap;
5  import java.util.Map;
6  import java.util.Set;
7  import java.util.TreeSet;
```

Pacchetto e import delle collezioni usate.

**Righe 9–10**

```java
 9  /** La risposta data dall'utente a una singola domanda. */
10  public final class Response {
```

Inizio della classe.

**Riga 12**

```java
12      private final Set<Integer> selected = new TreeSet<>();
```

Le opzioni scelte (0 = A, 1 = B…). `TreeSet` è un insieme **ordinato** e senza doppioni: confrontarlo con l'insieme delle risposte giuste diventa immediato.

**Riga 13**

```java
13      private final Map<Integer, String> dragAnswers = new HashMap<>();
```

Per le domande DRAG: posizione dell'elemento → categoria scelta.

**Riga 14**

```java
14      private String fillText = "";
```

Per le domande FILL: il testo digitato (inizialmente vuoto).

**Riga 16**

```java
16      public Set<Integer> getSelected() { return Collections.unmodifiableSet(selected); }
```

Restituisce le scelte in una "vetrina" non modificabile: chi la riceve può leggerla ma non cambiarla.

**Righe 18–23**

```java
18      public void select(int option, boolean single) {
19          if (single) {
20              selected.clear();
21          }
22          selected.add(option);
23      }
```

Seleziona un'opzione. Se la domanda è a risposta singola (`single` vero) prima svuota le scelte precedenti, come fa un pallino radio.

**Riga 25**

```java
25      public void deselect(int option) { selected.remove(option); }
```

Toglie un'opzione (casella deselezionata).

**Righe 27–29**

```java
27      public String getFillText() { return fillText; }
28  
29      public void setFillText(String text) { this.fillText = text == null ? "" : text; }
```

Getter e setter del testo libero; il setter trasforma un eventuale `null` in testo vuoto, per evitare errori più avanti.

**Riga 31**

```java
31      public String getDragAnswer(int item) { return dragAnswers.get(item); }
```

Categoria scelta per l'elemento `item` (o `null` se non ancora scelta).

**Righe 33–39**

```java
33      public void setDragAnswer(int item, String target) {
34          if (target == null || target.isEmpty()) {
35              dragAnswers.remove(item);
36          } else {
37              dragAnswers.put(item, target);
38          }
39      }
```

Memorizza un abbinamento; se la scelta è vuota (l'utente ha rimesso il menu sulla voce vuota) lo cancella.

**Righe 41–42**

```java
41      public int dragAnswerCount() { return dragAnswers.size(); }
42  }
```

Quanti elementi sono stati abbinati.

---

## DomainScore.java

`src/main/java/com/ccnaexamlab/model/DomainScore.java` — contatore corrette/totali per un dominio.

**Righe 1–3**

```java
1  package com.ccnaexamlab.model;
2  
3  /** Risultato parziale per un dominio. */
```

Pacchetto e commento.

**Righe 4–6**

```java
4  public final class DomainScore {
5      private int correct;
6      private int total;
```

Due contatori interi, inizialmente 0.

**Righe 8–13**

```java
 8      public DomainScore() { }
 9  
10      public DomainScore(int correct, int total) {
11          this.correct = correct;
12          this.total = total;
13      }
```

Due costruttori: uno vuoto (contatori a zero) e uno che riceve valori già pronti (usato quando si rilegge lo storico dal file).

**Righe 15–20**

```java
15      public void add(boolean ok) {
16          total++;
17          if (ok) {
18              correct++;
19          }
20      }
```

Registra una domanda: il totale cresce sempre di 1 (`total++`), le corrette solo se `ok` è vero.

**Righe 22–25**

```java
22      public void add(DomainScore other) {
23          correct += other.correct;
24          total += other.total;
25      }
```

Somma un altro punteggio a questo (usato per l'accuratezza complessiva nello storico). Due metodi con lo stesso nome ma parametri diversi: si chiama *overloading*.

**Righe 27–28**

```java
27      public int getCorrect() { return correct; }
28      public int getTotal() { return total; }
```

Getter.

**Righe 30–31**

```java
30      public double getPercent() { return total == 0 ? 0 : 100.0 * correct / total; }
31  }
```

Percentuale; se il totale è 0 restituisce 0 per evitare una divisione per zero. `100.0` (con il punto) forza un calcolo con la virgola.

---

## Question.java

`src/main/java/com/ccnaexamlab/model/Question.java` — la domanda d'esame, immutabile, con correzione e mescolamento.

**Righe 1–10**

```java
 1  package com.ccnaexamlab.model;
 2  
 3  import java.util.ArrayList;
 4  import java.util.Collections;
 5  import java.util.LinkedHashSet;
 6  import java.util.List;
 7  import java.util.Locale;
 8  import java.util.Objects;
 9  import java.util.Random;
10  import java.util.Set;
```

Pacchetto e import delle collezioni e utilità usate.

**Righe 12–13**

```java
12  /** Una domanda d'esame, immutabile. Si costruisce con {@link Builder}. */
13  public final class Question {
```

Inizio della classe `Question`.

**Righe 15–17**

```java
15      private static final String[] NUMBER_WORDS = {
16          "zero", "one", "two", "three", "four", "five", "six"
17      };
```

Array costante con i numeri in inglese, per scrivere "(Choose two.)".

**Righe 19–34**

```java
19      private final String id;
20      private final Domain domain;
21      private final QuestionType type;
22      private final String text;
23      private final String exhibit;
24      private final List<String> options;
25      private final Set<Integer> correctOptions;
26      private final List<String> acceptedAnswers;
27      private final List<String> dragItems;
28      private final List<String> dragTargets;
29      private final List<String> dragSolution;
30      private final String explanation;
31      private final String reference;
32      private final boolean generated;
33      private final boolean shuffleAllowed;
34      private final String source;
```

Tutti i campi della domanda. Sono tutti `final`: dopo la creazione non cambiano più (**immutabilità**). Il significato di ogni campo è in [03 - model](../03-classi-model.md#campi).

**Riga 36**

```java
36      private Question(Builder b) {
```

Costruttore **privato**: si può chiamare solo dal `Builder`. Copia i valori dal Builder.

**Righe 37–39**

```java
37          this.id = Objects.requireNonNull(b.id, "id");
38          this.domain = Objects.requireNonNull(b.domain, "domain");
39          this.type = Objects.requireNonNull(b.type, "type");
```

`requireNonNull` blocca subito la creazione se id, dominio o tipo mancano (sono indispensabili).

**Righe 40–41**

```java
40          this.text = b.text == null ? "" : b.text;
41          this.exhibit = b.exhibit == null ? "" : b.exhibit;
```

I testi facoltativi diventano stringhe vuote se mancano, così il resto del programma non deve mai preoccuparsi di `null`.

**Riga 42**

```java
42          this.options = List.copyOf(b.options);
```

`List.copyOf` crea una copia **non modificabile** della lista: anche se il Builder venisse riutilizzato, la domanda non cambierebbe.

**Riga 43**

```java
43          this.correctOptions = Collections.unmodifiableSet(new java.util.TreeSet<>(b.correctOptions));
```

Le risposte corrette in un insieme ordinato e non modificabile (l'ordine serve per mostrare "B, D" e non "D, B").

**Righe 44–46**

```java
44          this.acceptedAnswers = List.copyOf(b.acceptedAnswers);
45          this.dragItems = List.copyOf(b.dragItems);
46          this.dragSolution = List.copyOf(b.dragSolution);
```

Copie non modificabili di risposte accettate (FILL), elementi e soluzioni (DRAG).

**Righe 47–57**

```java
47          List<String> targets = new ArrayList<>();
48          for (String s : b.dragSolution) {
49              if (!targets.contains(s)) {
50                  targets.add(s);
51              }
52          }
53          for (String s : b.extraTargets) {
54              if (!targets.contains(s)) {
55                  targets.add(s);
56              }
57          }
```

Costruisce l'elenco delle categorie DRAG: prima tutte le soluzioni senza ripetizioni (`contains` controlla se ci sono già), poi le categorie extra ("distrattori").

**Righe 58–64**

```java
58          this.dragTargets = List.copyOf(targets);
59          this.explanation = b.explanation == null ? "" : b.explanation;
60          this.reference = b.reference == null ? "" : b.reference;
61          this.generated = b.generated;
62          this.shuffleAllowed = b.shuffleAllowed;
63          this.source = b.source == null ? "" : b.source;
64      }
```

Salva le categorie e i campi rimanenti, sempre sostituendo `null` con testo vuoto.

**Righe 66–83**

```java
66      public String getId() { return id; }
67      public Domain getDomain() { return domain; }
68      public QuestionType getType() { return type; }
69      public String getText() { return text; }
70      public String getExhibit() { return exhibit; }
71      public boolean hasExhibit() { return !exhibit.isBlank(); }
72      public List<String> getOptions() { return options; }
73      public Set<Integer> getCorrectOptions() { return correctOptions; }
74      public List<String> getAcceptedAnswers() { return acceptedAnswers; }
75      public List<String> getDragItems() { return dragItems; }
76      public List<String> getDragTargets() { return dragTargets; }
77      public List<String> getDragSolution() { return dragSolution; }
78      public String getExplanation() { return explanation; }
79      public String getReference() { return reference; }
80      public boolean isGenerated() { return generated; }
81      /** false se l'ordine delle opzioni è significativo (campo "shuffle: no"). */
82      public boolean isShuffleAllowed() { return shuffleAllowed; }
83      public String getSource() { return source; }
```

Getter: restituiscono i campi. `hasExhibit` dice se c'è un exhibit non vuoto (`isBlank` = vuoto o solo spazi).

**Righe 85–86**

```java
85      /** Numero di opzioni da selezionare. */
86      public int requiredSelections() { return correctOptions.size(); }
```

`requiredSelections`: quante opzioni bisogna scegliere = quante sono corrette.

**Righe 88–96**

```java
88      /** Testo da visualizzare: per le MULTI aggiunge "(Choose N.)" se manca. */
89      public String getDisplayText() {
90          if (type == QuestionType.MULTI && !text.toLowerCase(Locale.ROOT).contains("(choose")) {
91              int n = requiredSelections();
92              String word = n < NUMBER_WORDS.length ? NUMBER_WORDS[n] : String.valueOf(n);
93              return text + " (Choose " + word + ".)";
94          }
95          return text;
96      }
```

`getDisplayText`: se la domanda è MULTI e il testo non contiene già "(choose" (confronto in minuscolo), aggiunge "(Choose two.)" usando la parola inglese dall'array o, oltre il sei, la cifra.

**Righe 98–115**

```java
 98      /** true se l'utente ha fornito una risposta completa. */
 99      public boolean isAnswered(Response r) {
100          if (r == null) {
101              return false;
102          }
103          switch (type) {
104              case SINGLE:
105                  return r.getSelected().size() == 1;
106              case MULTI:
107                  return r.getSelected().size() == requiredSelections();
108              case FILL:
109                  return !r.getFillText().isBlank();
110              case DRAG:
111                  return r.dragAnswerCount() == dragItems.size();
112              default:
113                  return false;
114          }
115      }
```

`isAnswered`: una risposta mancante (`null`) non è completa; poi lo `switch` sceglie la regola in base al tipo: 1 scelta per SINGLE, esattamente N per MULTI, testo non vuoto per FILL, tutti gli elementi abbinati per DRAG. Il `default` serve solo per sicurezza.

**Righe 117–144**

```java
117      /** Correzione "tutto o niente", come all'esame Cisco. */
118      public boolean isCorrect(Response r) {
119          if (r == null) {
120              return false;
121          }
122          switch (type) {
123              case SINGLE:
124              case MULTI:
125                  return r.getSelected().equals(correctOptions);
126              case FILL:
127                  String given = normalize(r.getFillText());
128                  for (String a : acceptedAnswers) {
129                      if (normalize(a).equals(given)) {
130                          return true;
131                      }
132                  }
133                  return false;
134              case DRAG:
135                  for (int i = 0; i < dragItems.size(); i++) {
136                      if (!Objects.equals(r.getDragAnswer(i), dragSolution.get(i))) {
137                          return false;
138                      }
139                  }
140                  return true;
141              default:
142                  return false;
143          }
144      }
```

`isCorrect` (vedi [algoritmo 2](../08-algoritmi.md#2-correzione-di-una-risposta)). SINGLE e MULTI condividono lo stesso caso: l'insieme scelto deve essere uguale (`equals`) a quello corretto. FILL: normalizza il testo e lo confronta con ogni risposta accettata normalizzata. DRAG: ogni elemento deve avere la categoria giusta; `Objects.equals` gestisce anche il caso "non abbinato" (`null`).

**Righe 146–171**

```java
146      /** Descrizione testuale della risposta corretta. */
147      public String getCorrectAnswerText() {
148          StringBuilder sb = new StringBuilder();
149          switch (type) {
150              case SINGLE:
151              case MULTI:
152                  for (int i : correctOptions) {
153                      if (sb.length() > 0) {
154                          sb.append(", ");
155                      }
156                      sb.append(letter(i));
157                  }
158                  break;
159              case FILL:
160                  sb.append(String.join("  oppure  ", acceptedAnswers));
161                  break;
162              case DRAG:
163                  for (int i = 0; i < dragItems.size(); i++) {
164                      sb.append(dragItems.get(i)).append(" → ").append(dragSolution.get(i)).append('\n');
165                  }
166                  break;
167              default:
168                  break;
169          }
170          return sb.toString().trim();
171      }
```

`getCorrectAnswerText` costruisce il testo con uno `StringBuilder` (un "foglio" su cui aggiungere pezzi): lettere separate da virgola per SINGLE/MULTI, alternative unite da "oppure" per FILL, una riga "elemento → categoria" per DRAG. `trim()` toglie l'ultimo a capo.

**Righe 173–175**

```java
173      public static char letter(int index) {
174          return (char) ('A' + index);
175      }
```

Converte una posizione in lettera: i caratteri sono numeri, quindi `'A' + 1` è `'B'`. Il `(char)` riconverte il numero in carattere.

**Righe 177–183**

```java
177      /** Normalizza una risposta libera: minuscolo, spazi compattati. */
178      public static String normalize(String s) {
179          if (s == null) {
180              return "";
181          }
182          return s.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
183      }
```

`normalize`: toglie gli spazi agli estremi, mette in minuscolo (con `Locale.ROOT` per non dipendere dalla lingua del computer) e sostituisce ogni sequenza di spazi con un solo spazio (`\\s+` è un'espressione regolare che significa "uno o più spazi").

**Righe 185–189**

```java
185      /**
186       * Restituisce una copia della domanda con le opzioni (SINGLE/MULTI) in ordine casuale,
187       * così la posizione della risposta corretta non si può memorizzare. Le altre tipologie,
188       * o le domande con "shuffle: no", vengono restituite invariate.
189       */
```

Commento del metodo `shuffled`.

**Righe 190–211**

```java
190      public Question shuffled(Random random) {
191          if ((type != QuestionType.SINGLE && type != QuestionType.MULTI) || !shuffleAllowed) {
192              return this;
193          }
194          List<Integer> perm = new ArrayList<>();
195          for (int i = 0; i < options.size(); i++) {
196              perm.add(i);
197          }
198          Collections.shuffle(perm, random);
199          Builder b = new Builder()
200              .id(id).domain(domain).type(type).text(text).exhibit(exhibit)
201              .explanation(explanation).reference(reference).generated(generated)
202              .shuffleAllowed(shuffleAllowed).source(source);
203          for (int newIdx = 0; newIdx < perm.size(); newIdx++) {
204              int oldIdx = perm.get(newIdx);
205              b.option(options.get(oldIdx));
206              if (correctOptions.contains(oldIdx)) {
207                  b.correct(newIdx);
208              }
209          }
210          return b.build();
211      }
```

Crea una copia con le opzioni mescolate (vedi [algoritmo 6](../08-algoritmi.md#6-mescolamento-delle-opzioni)). Se il tipo non è SINGLE/MULTI o il mescolamento non è consentito restituisce la domanda stessa (`this`). Altrimenti crea la lista delle posizioni 0…n-1, la mescola, prepara un Builder con tutti i dati invariati e, per ogni nuova posizione, copia l'opzione originale corrispondente segnando come corretta la nuova posizione se l'originale lo era. Infine costruisce la nuova domanda (passando di nuovo per i controlli di `build`).

**Righe 213–214**

```java
213      @Override
214      public String toString() { return id; }
```

`toString` restituisce l'ID: utile nei messaggi di debug.

**Righe 216–217**

```java
216      /** Builder mutabile usato dal parser e dal generatore. */
217      public static final class Builder {
```

Inizio della classe interna **`Builder`** (`static` = non ha bisogno di una domanda esistente). È il "modulo da compilare" prima di creare la domanda.

**Righe 218–233**

```java
218          private String id;
219          private Domain domain;
220          private QuestionType type;
221          private String text;
222          private String exhibit;
223          private final List<String> options = new ArrayList<>();
224          private final Set<Integer> correctOptions = new LinkedHashSet<>();
225          private final List<String> acceptedAnswers = new ArrayList<>();
226          private final List<String> dragItems = new ArrayList<>();
227          private final List<String> dragSolution = new ArrayList<>();
228          private final List<String> extraTargets = new ArrayList<>();
229          private String explanation;
230          private String reference;
231          private boolean generated;
232          private boolean shuffleAllowed = true;
233          private String source;
```

I campi del Builder: gli stessi della domanda, ma **modificabili**. Le liste partono vuote; `shuffleAllowed` parte da `true`. `extraTargets` contiene le categorie distrattrici DRAG.

**Righe 235–242**

```java
235          public Builder id(String v) { this.id = v; return this; }
236          public Builder domain(Domain v) { this.domain = v; return this; }
237          public Builder type(QuestionType v) { this.type = v; return this; }
238          public Builder text(String v) { this.text = v; return this; }
239          public Builder exhibit(String v) { this.exhibit = v; return this; }
240          public Builder option(String v) { this.options.add(v); return this; }
241          public Builder correct(int index) { this.correctOptions.add(index); return this; }
242          public Builder accept(String v) { this.acceptedAnswers.add(v); return this; }
```

Metodi di impostazione: ognuno salva un valore e restituisce il Builder stesso (`return this`), così si possono scrivere a catena: `.id("X").domain(...).text(...)`. `option`, `correct` e `accept` **aggiungono** a una lista invece di sostituire.

**Righe 243–247**

```java
243          public Builder dragPair(String item, String target) {
244              this.dragItems.add(item);
245              this.dragSolution.add(target);
246              return this;
247          }
```

Per le DRAG aggiunge insieme un elemento e la sua categoria giusta, così le due liste restano allineate (stessa posizione).

**Righe 248–253**

```java
248          public Builder extraTarget(String v) { this.extraTargets.add(v); return this; }
249          public Builder explanation(String v) { this.explanation = v; return this; }
250          public Builder reference(String v) { this.reference = v; return this; }
251          public Builder generated(boolean v) { this.generated = v; return this; }
252          public Builder shuffleAllowed(boolean v) { this.shuffleAllowed = v; return this; }
253          public Builder source(String v) { this.source = v; return this; }
```

Altri metodi di impostazione: categorie extra, spiegazione, riferimento, flag "generata", flag "mescolabile", file di origine.

**Riga 255**

```java
255          public String getId() { return id; }
```

Permette di leggere l'ID anche prima della costruzione.

**Righe 257–258**

```java
257          /** Valida la coerenza della domanda e la costruisce. */
258          public Question build() {
```

`build()`: i controlli di coerenza. Ogni `throw new IllegalStateException(...)` interrompe la costruzione con un messaggio che contiene l'ID: il parser lo mostrerà all'utente.

**Righe 259–270**

```java
259              if (id == null || id.isBlank()) {
260                  throw new IllegalStateException("id mancante");
261              }
262              if (domain == null) {
263                  throw new IllegalStateException(id + ": domain mancante");
264              }
265              if (type == null) {
266                  throw new IllegalStateException(id + ": type mancante");
267              }
268              if (text == null || text.isBlank()) {
269                  throw new IllegalStateException(id + ": text mancante");
270              }
```

Controlli comuni a tutti i tipi: ID, dominio, tipo e testo devono esserci.

**Righe 271–301**

```java
271              switch (type) {
272                  case SINGLE:
273                  case MULTI:
274                      if (options.size() < 2) {
275                          throw new IllegalStateException(id + ": servono almeno 2 opzioni");
276                      }
277                      for (int c : correctOptions) {
278                          if (c < 0 || c >= options.size()) {
279                              throw new IllegalStateException(id + ": risposta fuori dalle opzioni");
280                          }
281                      }
282                      if (type == QuestionType.SINGLE && correctOptions.size() != 1) {
283                          throw new IllegalStateException(id + ": SINGLE richiede esattamente 1 risposta");
284                      }
285                      if (type == QuestionType.MULTI && correctOptions.size() < 2) {
286                          throw new IllegalStateException(id + ": MULTI richiede almeno 2 risposte");
287                      }
288                      break;
289                  case FILL:
290                      if (acceptedAnswers.isEmpty()) {
291                          throw new IllegalStateException(id + ": FILL richiede almeno una risposta");
292                      }
293                      break;
294                  case DRAG:
295                      if (dragItems.size() < 2) {
296                          throw new IllegalStateException(id + ": DRAG richiede almeno 2 elementi");
297                      }
298                      break;
299                  default:
300                      break;
301              }
```

Controlli specifici per tipo: SINGLE/MULTI almeno 2 opzioni, ogni risposta deve essere una posizione valida (tra 0 e il numero di opzioni − 1), SINGLE esattamente una risposta, MULTI almeno due; FILL almeno una risposta accettata; DRAG almeno due elementi. `break` esce dallo `switch`.

**Riga 302**

```java
302              return new Question(this);
```

Tutto in regola: crea la domanda passando sé stesso al costruttore privato.

**Righe 303–305**

```java
303          }
304      }
305  }
```

Chiusura della classe interna `Builder` e della classe `Question`.

---

## ExamSession.java

`src/main/java/com/ccnaexamlab/model/ExamSession.java` — l'esame in corso.

**Righe 1–5**

```java
1  package com.ccnaexamlab.model;
2  
3  import java.util.List;
4  
5  /** Stato di un esame in corso. */
```

Pacchetto, import e commento.

**Riga 6**

```java
6  public final class ExamSession {
```

Inizio della classe.

**Righe 8–15**

```java
 8      private final String title;
 9      private final ExamMode mode;
10      private final List<Question> questions;
11      private final Response[] responses;
12      private final boolean[] marked;
13      private final boolean[] revealed;
14      private final int timeLimitSeconds;
15      private final boolean allowBack;
```

Campi fissi dell'esame: titolo, modalità, domande, e tre array paralleli (risposte, marcature, rivelazioni) lunghi quanto le domande, limite di tempo e possibilità di tornare indietro.

**Righe 16–17**

```java
16      private int elapsedSeconds;
17      private int current;
```

Campi che cambiano durante l'esame: secondi trascorsi e posizione della domanda corrente.

**Righe 19–22**

```java
19      /**
20       * @param timeLimitSeconds 0 = nessun limite
21       * @param allowBack se false non si può tornare alla domanda precedente (come all'esame Cisco)
22       */
```

Commento del costruttore con il significato dei parametri.

**Righe 23–39**

```java
23      public ExamSession(String title, ExamMode mode, List<Question> questions,
24                         int timeLimitSeconds, boolean allowBack) {
25          if (questions.isEmpty()) {
26              throw new IllegalArgumentException("L'esame non contiene domande");
27          }
28          this.title = title;
29          this.mode = mode;
30          this.questions = List.copyOf(questions);
31          this.responses = new Response[questions.size()];
32          for (int i = 0; i < responses.length; i++) {
33              responses[i] = new Response();
34          }
35          this.marked = new boolean[questions.size()];
36          this.revealed = new boolean[questions.size()];
37          this.timeLimitSeconds = timeLimitSeconds;
38          this.allowBack = allowBack;
39      }
```

Costruttore. Rifiuta un esame senza domande (eccezione). Copia la lista domande in versione non modificabile, crea un array di risposte e riempie ogni posizione con una `Response` nuova e vuota. Gli array `boolean` nascono già pieni di `false`.

**Righe 41–54**

```java
41      public String getTitle() { return title; }
42      public ExamMode getMode() { return mode; }
43      public List<Question> getQuestions() { return questions; }
44      public int size() { return questions.size(); }
45      public Question getQuestion(int i) { return questions.get(i); }
46      public Response getResponse(int i) { return responses[i]; }
47      public boolean isMarked(int i) { return marked[i]; }
48      public void setMarked(int i, boolean v) { marked[i] = v; }
49      public boolean isRevealed(int i) { return revealed[i]; }
50      public void setRevealed(int i, boolean v) { revealed[i] = v; }
51      public int getTimeLimitSeconds() { return timeLimitSeconds; }
52      public boolean isTimed() { return timeLimitSeconds > 0; }
53      public boolean isAllowBack() { return allowBack; }
54      public int getElapsedSeconds() { return elapsedSeconds; }
```

Getter e setter elementari: titolo, modalità, domande, numero di domande (`size`), domanda e risposta in posizione `i`, marcatura e rivelazione, limite di tempo, `isTimed` (vero se il limite è positivo), possibilità di tornare indietro, secondi trascorsi.

**Riga 55**

```java
55      public void tick() { elapsedSeconds++; }
```

Chiamato dal timer ogni secondo: aggiunge un secondo.

**Riga 56**

```java
56      public int getRemainingSeconds() { return Math.max(0, timeLimitSeconds - elapsedSeconds); }
```

Secondi rimanenti, mai negativi (`Math.max` prende il più grande tra 0 e la differenza).

**Riga 57**

```java
57      public int getCurrent() { return current; }
```

Indice della domanda corrente.

**Righe 59–63**

```java
59      public void setCurrent(int i) {
60          if (i >= 0 && i < questions.size()) {
61              current = i;
62          }
63      }
```

Cambia domanda corrente solo se l'indice è valido (tra 0 e il numero di domande − 1).

**Riga 65**

```java
65      public boolean isAnswered(int i) { return questions.get(i).isAnswered(responses[i]); }
```

Chiede alla domanda `i` se la sua risposta è completa.

**Righe 67–75**

```java
67      public int countAnswered() {
68          int n = 0;
69          for (int i = 0; i < size(); i++) {
70              if (isAnswered(i)) {
71                  n++;
72              }
73          }
74          return n;
75      }
```

Conta le domande con risposta completa scorrendole tutte.

**Righe 77–86**

```java
77      public int countMarked() {
78          int n = 0;
79          for (boolean m : marked) {
80              if (m) {
81                  n++;
82              }
83          }
84          return n;
85      }
86  }
```

Conta le domande marcate (`for (boolean m : marked)` = "per ogni valore dell'array").

---

## ExamResult.java

`src/main/java/com/ccnaexamlab/model/ExamResult.java` — il risultato di un esame concluso.

**Righe 1–7**

```java
1  package com.ccnaexamlab.model;
2  
3  import java.time.LocalDateTime;
4  import java.util.EnumMap;
5  import java.util.Map;
6  
7  /** Esito di un esame concluso. */
```

Pacchetto, import (data/ora e mappe) e commento.

**Riga 8**

```java
8  public final class ExamResult {
```

Inizio della classe.

**Righe 10–13**

```java
10      /** Punteggio minimo (scala 300-1000) considerato di superamento. */
11      public static final int PASSING_SCORE = 825;
12      public static final int MIN_SCORE = 300;
13      public static final int MAX_SCORE = 1000;
```

Costanti della scala: soglia 825, minimo 300, massimo 1000.

**Righe 15–21**

```java
15      private final String title;
16      private final ExamMode mode;
17      private final LocalDateTime date;
18      private final int correct;
19      private final int total;
20      private final int durationSeconds;
21      private final Map<Domain, DomainScore> byDomain;
```

Campi del risultato: titolo, modalità, data, corrette, totale, durata in secondi, punteggi per dominio.

**Righe 22–23**

```java
22      /** La sessione è disponibile solo per l'esame appena svolto (non viene salvata). */
23      private final ExamSession session;
```

Riferimento alla sessione per la revisione; vale `null` per i risultati riletti dallo storico.

**Righe 25–36**

```java
25      public ExamResult(String title, ExamMode mode, LocalDateTime date, int correct, int total,
26                        int durationSeconds, Map<Domain, DomainScore> byDomain, ExamSession session) {
27          this.title = title;
28          this.mode = mode;
29          this.date = date;
30          this.correct = correct;
31          this.total = total;
32          this.durationSeconds = durationSeconds;
33          this.byDomain = new EnumMap<>(Domain.class);
34          this.byDomain.putAll(byDomain);
35          this.session = session;
36      }
```

Costruttore: copia i valori; la mappa per dominio viene ricopiata in una `EnumMap`, che mantiene i domini nell'ordine 1.0 → 6.0.

**Righe 38–45**

```java
38      public String getTitle() { return title; }
39      public ExamMode getMode() { return mode; }
40      public LocalDateTime getDate() { return date; }
41      public int getCorrect() { return correct; }
42      public int getTotal() { return total; }
43      public int getDurationSeconds() { return durationSeconds; }
44      public Map<Domain, DomainScore> getByDomain() { return byDomain; }
45      public ExamSession getSession() { return session; }
```

Getter.

**Riga 47**

```java
47      public double getPercent() { return total == 0 ? 0 : 100.0 * correct / total; }
```

Percentuale di risposte corrette.

**Righe 49–55**

```java
49      /** Punteggio scalato stimato su scala 300-1000. */
50      public int getScaledScore() {
51          if (total == 0) {
52              return MIN_SCORE;
53          }
54          return (int) Math.round(MIN_SCORE + (MAX_SCORE - MIN_SCORE) * ((double) correct / total));
55      }
```

`getScaledScore`: 300 + 700 × (corrette/totale), arrotondato (`Math.round`) e convertito in intero. Con 0 domande restituisce il minimo. Vedi [algoritmo 5](../08-algoritmi.md#5-calcolo-del-punteggio).

**Righe 57–58**

```java
57      public boolean isPassed() { return getScaledScore() >= PASSING_SCORE; }
58  }
```

Superato se il punteggio raggiunge almeno la soglia.
