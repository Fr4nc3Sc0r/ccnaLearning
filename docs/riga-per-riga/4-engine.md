# Riga per riga — pacchetto `engine`

Il pacchetto `engine` prepara gli esami (`ExamFactory`, con `ExamConfig`, `QuestionFilter` e `PresetExam`) e li corregge (`Scorer`). Descrizione generale: [05 - engine](../05-classi-engine.md).

**File in questo documento:** [`QuestionFilter.java`](#questionfilterjava) · [`ExamConfig.java`](#examconfigjava) · [`PresetExam.java`](#presetexamjava) · [`ExamFactory.java`](#examfactoryjava) · [`Scorer.java`](#scorerjava)

---

## QuestionFilter.java

`src/main/java/com/lordoc/engine/QuestionFilter.java` — i filtri per scegliere le domande.

**Righe 1–3**

```java
1  package com.lordoc.engine;
2  
3  /** Filtro sulle domande da includere in un esame personalizzato. */
```

Pacchetto e commento.

**Righe 4–9**

```java
4  public enum QuestionFilter {
5      ALL("Tutte le domande"),
6      UNSEEN("Solo domande mai viste"),
7      MISSED("Solo domande sbagliate almeno una volta"),
8      LAST_WRONG("Solo domande sbagliate l'ultima volta"),
9      FLAGGED("Solo domande marcate");
```

I cinque filtri, ognuno con l'etichetta mostrata nel menu a tendina della finestra "Esame personalizzato".

**Righe 11–13**

```java
11      private final String label;
12  
13      QuestionFilter(String label) { this.label = label; }
```

Campo e costruttore dell'etichetta.

**Righe 15–17**

```java
15      @Override
16      public String toString() { return label; }
17  }
```

`toString` restituisce l'etichetta, così il menu a tendina mostra il testo in italiano.

---

## ExamConfig.java

`src/main/java/com/lordoc/engine/ExamConfig.java` — i parametri di un esame personalizzato.

**Righe 1–7**

```java
1  package com.lordoc.engine;
2  
3  import com.lordoc.model.Domain;
4  import com.lordoc.model.ExamMode;
5  
6  import java.util.EnumSet;
7  import java.util.Set;
```

Pacchetto e import.

**Righe 9–10**

```java
 9  /** Parametri per la creazione di un esame personalizzato. */
10  public final class ExamConfig {
```

Inizio della classe.

**Righe 11–19**

```java
11      private String title = "Esame personalizzato";
12      private ExamMode mode = ExamMode.STUDY;
13      private Set<Domain> domains = EnumSet.allOf(Domain.class);
14      private int questionCount = 30;
15      private QuestionFilter filter = QuestionFilter.ALL;
16      private int subnetQuestions;
17      private int timeMinutes;
18      private boolean allowBack = true;
19      private boolean weighted = true;
```

I parametri con i loro **valori di default**: titolo, modalità Studio, tutti i domini (`EnumSet.allOf`), 30 domande, nessun filtro, nessuna domanda di subnetting, tempo illimitato (0), ritorno indietro consentito, distribuzione pesata.

**Righe 21–32**

```java
21      public String getTitle() { return title; }
22      public ExamConfig title(String v) { this.title = v; return this; }
23      public ExamMode getMode() { return mode; }
24      public ExamConfig mode(ExamMode v) { this.mode = v; return this; }
25      public Set<Domain> getDomains() { return domains; }
26      public ExamConfig domains(Set<Domain> v) { this.domains = EnumSet.copyOf(v); return this; }
27      public int getQuestionCount() { return questionCount; }
28      public ExamConfig questionCount(int v) { this.questionCount = v; return this; }
29      public QuestionFilter getFilter() { return filter; }
30      public ExamConfig filter(QuestionFilter v) { this.filter = v; return this; }
31      public int getSubnetQuestions() { return subnetQuestions; }
32      public ExamConfig subnetQuestions(int v) { this.subnetQuestions = v; return this; }
```

Coppie getter/setter. I setter hanno lo stesso nome del campo e restituiscono `this` (stile *fluente*), per poterli concatenare. `domains` copia l'insieme ricevuto (`EnumSet.copyOf`) così chi l'ha passato non può modificarlo dopo.

**Righe 33–35**

```java
33      /** 0 = senza limite di tempo. */
34      public int getTimeMinutes() { return timeMinutes; }
35      public ExamConfig timeMinutes(int v) { this.timeMinutes = v; return this; }
```

Minuti a disposizione (0 = illimitato).

**Righe 36–37**

```java
36      public boolean isAllowBack() { return allowBack; }
37      public ExamConfig allowBack(boolean v) { this.allowBack = v; return this; }
```

Ritorno indietro consentito.

**Righe 38–41**

```java
38      /** Se true le domande vengono distribuite secondo i pesi dei domini del blueprint. */
39      public boolean isWeighted() { return weighted; }
40      public ExamConfig weighted(boolean v) { this.weighted = v; return this; }
41  }
```

Distribuzione pesata sì/no.

---

## PresetExam.java

`src/main/java/com/lordoc/engine/PresetExam.java` — un esame predefinito (nome + ID delle domande).

**Righe 1–3**

```java
1  package com.lordoc.engine;
2  
3  import java.util.List;
```

Pacchetto e import.

**Righe 5–6**

```java
5  /** Un esame predefinito (insieme fisso di domande), come "Esame A", "Esame B"... */
6  public final class PresetExam {
```

Inizio della classe.

**Righe 7–8**

```java
7      private final String name;
8      private final List<String> questionIds;
```

Nome e lista degli ID delle domande.

**Righe 10–13**

```java
10      public PresetExam(String name, List<String> questionIds) {
11          this.name = name;
12          this.questionIds = List.copyOf(questionIds);
13      }
```

Costruttore: salva il nome e una copia non modificabile della lista.

**Righe 15–18**

```java
15      public String getName() { return name; }
16      public List<String> getQuestionIds() { return questionIds; }
17      public int size() { return questionIds.size(); }
18  }
```

Getter e `size()` (numero di domande).

---

## ExamFactory.java

`src/main/java/com/lordoc/engine/ExamFactory.java` — la costruzione di tutti i tipi di esame.

**Righe 1–18**

```java
 1  package com.lordoc.engine;
 2  
 3  import com.lordoc.data.QuestionBank;
 4  import com.lordoc.data.QuestionStat;
 5  import com.lordoc.data.SubnetQuestionGenerator;
 6  import com.lordoc.data.UserDataStore;
 7  import com.lordoc.model.Domain;
 8  import com.lordoc.model.ExamMode;
 9  import com.lordoc.model.ExamSession;
10  import com.lordoc.model.Question;
11  
12  import java.util.ArrayList;
13  import java.util.Collections;
14  import java.util.EnumMap;
15  import java.util.List;
16  import java.util.Map;
17  import java.util.Random;
18  import java.util.Set;
```

Pacchetto e import: banca, statistiche, generatore, modello e collezioni Java.

**Righe 20–21**

```java
20  /** Costruisce gli esami: predefiniti, casuali e personalizzati. */
21  public final class ExamFactory {
```

Inizio della classe.

**Righe 23–27**

```java
23      /** Dimensione indicativa di un esame predefinito. */
24      public static final int PRESET_SIZE = 70;
25      /** Minuti per domanda (l'esame reale: ~120 minuti per ~100 domande). */
26      public static final double MINUTES_PER_QUESTION = 1.2;
27      private static final long PRESET_SEED = 200301L;
```

Costanti: dimensione degli esami predefiniti (70), minuti per domanda (1,2) e il seme fisso usato per suddividerli sempre allo stesso modo.

**Righe 29–31**

```java
29      private final QuestionBank bank;
30      private final UserDataStore store;
31      private final Random random = new Random();
```

Riferimenti alla banca e alle statistiche, più un generatore casuale **senza seme** (diverso a ogni avvio) per esami casuali e mescolamenti.

**Righe 33–36**

```java
33      public ExamFactory(QuestionBank bank, UserDataStore store) {
34          this.bank = bank;
35          this.store = store;
36      }
```

Costruttore: salva i due servizi.

**Righe 38–40**

```java
38      public static int suggestedMinutes(int questions) {
39          return (int) Math.ceil(questions * MINUTES_PER_QUESTION);
40      }
```

Minuti consigliati: domande × 1,2, arrotondato per eccesso (`Math.ceil`).

**Righe 42–45**

```java
42      /**
43       * Suddivide in modo deterministico la banca domande in esami disgiunti, ognuno con
44       * una distribuzione per dominio simile a quella dell'esame reale.
45       */
```

Commento di `presetExams`.

**Righe 46–51**

```java
46      public List<PresetExam> presetExams() {
47          int n = Math.max(1, bank.size() / PRESET_SIZE);
48          List<List<String>> buckets = new ArrayList<>();
49          for (int i = 0; i < n; i++) {
50              buckets.add(new ArrayList<>());
51          }
```

Suddivisione della banca in esami disgiunti ([algoritmo 3](../08-algoritmi.md#3-suddivisione-negli-esami-predefiniti)). `n` = numero di esami (divisione intera, minimo 1); si creano `n` liste vuote.

**Righe 52–61**

```java
52          Random r = new Random(PRESET_SEED);
53          int next = 0;
54          for (Domain d : Domain.values()) {
55              List<Question> qs = bank.byDomain(d);
56              Collections.shuffle(qs, r);
57              for (Question q : qs) {
58                  buckets.get(next % n).add(q.getId());
59                  next++;
60              }
61          }
```

Generatore con seme fisso: sequenza "casuale" sempre identica. Per ogni dominio, nell'ordine dell'enum, mescola le sue domande e le distribuisce a giro tra le liste: `next % n` è il resto della divisione, che cicla 0, 1, …, n−1, 0, 1…

**Righe 62–69**

```java
62          List<PresetExam> exams = new ArrayList<>();
63          for (int i = 0; i < n; i++) {
64              List<String> ids = buckets.get(i);
65              Collections.shuffle(ids, r);
66              exams.add(new PresetExam("Esame " + (char) ('A' + i), ids));
67          }
68          return exams;
69      }
```

Per ogni lista mescola l'ordine degli ID e crea l'esame chiamandolo "Esame A", "Esame B"… (`'A' + i` convertito in carattere).

**Righe 71–81**

```java
71      public ExamSession fromPreset(PresetExam preset, ExamMode mode, boolean allowBack) {
72          List<Question> qs = new ArrayList<>();
73          for (String id : preset.getQuestionIds()) {
74              Question q = bank.get(id);
75              if (q != null) {
76                  qs.add(q.shuffled(random));
77              }
78          }
79          int time = mode == ExamMode.SIMULATION ? suggestedMinutes(qs.size()) * 60 : 0;
80          return new ExamSession(preset.getName(), mode, qs, time, allowBack);
81      }
```

Avvia un esame predefinito: per ogni ID recupera la domanda dalla banca (se esiste ancora) e ne mette una copia con le opzioni mescolate. Il tempo è assegnato solo in Simulazione (minuti × 60 = secondi); in Studio vale 0, cioè illimitato.

**Riga 83**

```java
83      /** Esame personalizzato. Restituisce null se nessuna domanda soddisfa i criteri. */
```

Commento di `custom`.

**Righe 84–90**

```java
84      public ExamSession custom(ExamConfig cfg) {
85          List<Question> pool = new ArrayList<>();
86          for (Question q : bank.all()) {
87              if (cfg.getDomains().contains(q.getDomain()) && matches(q, cfg.getFilter())) {
88                  pool.add(q);
89              }
90          }
```

Il "pool": tutte le domande dei domini scelti che passano il filtro.

**Righe 91–93**

```java
91          List<Question> chosen = cfg.isWeighted()
92              ? weightedPick(pool, cfg.getDomains(), cfg.getQuestionCount())
93              : randomPick(pool, cfg.getQuestionCount());
```

Selezione pesata o casuale a seconda della configurazione (operatore `? :`).

**Riga 94**

```java
94          chosen.replaceAll(q -> q.shuffled(random));
```

Sostituisce ogni domanda con la sua copia a opzioni mescolate.

**Righe 95–97**

```java
95          if (cfg.getSubnetQuestions() > 0) {
96              chosen.addAll(new SubnetQuestionGenerator(random).generate(cfg.getSubnetQuestions()));
97          }
```

Aggiunge le domande di subnetting generate, se richieste.

**Righe 98–100**

```java
 98          if (chosen.isEmpty()) {
 99              return null;
100          }
```

Nessuna domanda: restituisce `null` e sarà l'interfaccia a mostrare un avviso.

**Righe 101–103**

```java
101          Collections.shuffle(chosen, random);
102          return new ExamSession(cfg.getTitle(), cfg.getMode(), chosen, cfg.getTimeMinutes() * 60, cfg.isAllowBack());
103      }
```

Mescola l'ordine finale (così domande generate e domini sono mischiati) e crea la sessione con titolo, modalità, tempo in secondi e regola sul ritorno indietro.

**Righe 105–114**

```java
105      /** Esame costruito su una lista precisa di domande (es. "ripeti gli errori"). */
106      public ExamSession fromQuestions(String title, ExamMode mode, List<Question> questions) {
107          List<Question> qs = new ArrayList<>();
108          for (Question q : questions) {
109              qs.add(q.shuffled(random));
110          }
111          Collections.shuffle(qs, random);
112          int time = mode == ExamMode.SIMULATION ? suggestedMinutes(qs.size()) * 60 : 0;
113          return new ExamSession(title, mode, qs, time, true);
114      }
```

`fromQuestions`: crea un esame da una lista data (es. le domande sbagliate), mescolando opzioni e ordine; il ritorno indietro è sempre consentito.

**Righe 116–125**

```java
116      private boolean matches(Question q, QuestionFilter f) {
117          QuestionStat s = store.peekStat(q.getId());
118          switch (f) {
119              case UNSEEN: return s == null || s.getSeen() == 0;
120              case MISSED: return s != null && s.getWrong() > 0;
121              case LAST_WRONG: return s != null && s.getSeen() > 0 && !s.isLastCorrect();
122              case FLAGGED: return s != null && s.isFlagged();
123              default: return true;
124          }
125      }
```

Applica il filtro leggendo la statistica della domanda senza crearla (`peekStat`). Ogni `case` restituisce direttamente il risultato: mai vista (nessuna statistica o 0 visualizzazioni), sbagliata almeno una volta, sbagliata l'ultima volta, marcata; `default` (ALL) accetta tutto.

**Righe 127–131**

```java
127      private List<Question> randomPick(List<Question> pool, int count) {
128          List<Question> copy = new ArrayList<>(pool);
129          Collections.shuffle(copy, random);
130          return new ArrayList<>(copy.subList(0, Math.min(count, copy.size())));
131      }
```

Selezione casuale: copia il pool, lo mescola e prende i primi `count` elementi (o tutti, se sono meno). `subList` è una "finestra" sulla lista; la si ricopia in una lista nuova e indipendente.

**Riga 133**

```java
133      /** Selezione proporzionale ai pesi dei domini; i posti non coperti vengono riempiti a caso. */
```

Commento di `weightedPick` ([algoritmo 4](../08-algoritmi.md#4-esame-personalizzato-e-selezione-pesata)).

**Righe 134–138**

```java
134      private List<Question> weightedPick(List<Question> pool, Set<Domain> domains, int count) {
135          Map<Domain, List<Question>> byDomain = new EnumMap<>(Domain.class);
136          for (Question q : pool) {
137              byDomain.computeIfAbsent(q.getDomain(), k -> new ArrayList<>()).add(q);
138          }
```

Raggruppa il pool per dominio: `computeIfAbsent` crea la lista del dominio la prima volta che serve, poi vi aggiunge la domanda.

**Righe 139–142**

```java
139          int totalWeight = 0;
140          for (Domain d : domains) {
141              totalWeight += d.getWeight();
142          }
```

Somma dei pesi dei soli domini scelti (se scegli due domini da 20 e 25, il totale è 45).

**Righe 143–144**

```java
143          List<Question> chosen = new ArrayList<>();
144          List<Question> leftovers = new ArrayList<>();
```

Due liste: le domande scelte e quelle "avanzate".

**Righe 145–152**

```java
145          for (Domain d : domains) {
146              List<Question> qs = byDomain.getOrDefault(d, new ArrayList<>());
147              Collections.shuffle(qs, random);
148              int quota = totalWeight == 0 ? 0 : (int) Math.round((double) count * d.getWeight() / totalWeight);
149              int take = Math.min(quota, qs.size());
150              chosen.addAll(qs.subList(0, take));
151              leftovers.addAll(qs.subList(take, qs.size()));
152          }
```

Per ogni dominio: mescola le sue domande, calcola la quota proporzionale al peso (arrotondata), prende il minimo tra quota e disponibili, mette le prime nelle scelte e le rimanenti nelle avanzate. `getOrDefault` evita errori se il dominio non ha domande.

**Righe 153–159**

```java
153          Collections.shuffle(leftovers, random);
154          for (Question q : leftovers) {
155              if (chosen.size() >= count) {
156                  break;
157              }
158              chosen.add(q);
159          }
```

Riempie gli eventuali posti mancanti con avanzate casuali, fermandosi (`break`) quando si raggiunge il numero richiesto.

**Righe 160–165**

```java
160          while (chosen.size() > count) {
161              chosen.remove(chosen.size() - 1);
162          }
163          return chosen;
164      }
165  }
```

Se gli arrotondamenti hanno superato il numero richiesto, toglie le ultime domande in eccesso.

---

## Scorer.java

`src/main/java/com/lordoc/engine/Scorer.java` — la correzione dell'esame.

**Righe 1–11**

```java
 1  package com.lordoc.engine;
 2  
 3  import com.lordoc.model.Domain;
 4  import com.lordoc.model.DomainScore;
 5  import com.lordoc.model.ExamResult;
 6  import com.lordoc.model.ExamSession;
 7  import com.lordoc.model.Question;
 8  
 9  import java.time.LocalDateTime;
10  import java.util.EnumMap;
11  import java.util.Map;
```

Pacchetto e import.

**Righe 13–16**

```java
13  /** Corregge un esame. */
14  public final class Scorer {
15  
16      private Scorer() { }
```

Inizio della classe; costruttore privato perché contiene solo un metodo statico.

**Righe 18–20**

```java
18      public static ExamResult score(ExamSession s) {
19          Map<Domain, DomainScore> byDomain = new EnumMap<>(Domain.class);
20          int correct = 0;
```

Prepara una mappa vuota dominio → punteggio e il contatore delle corrette.

**Righe 21–28**

```java
21          for (int i = 0; i < s.size(); i++) {
22              Question q = s.getQuestion(i);
23              boolean ok = q.isCorrect(s.getResponse(i));
24              if (ok) {
25                  correct++;
26              }
27              byDomain.computeIfAbsent(q.getDomain(), k -> new DomainScore()).add(ok);
28          }
```

Per ogni domanda: chiede se la risposta è corretta, incrementa il contatore se sì, e aggiunge l'esito al punteggio del suo dominio (creandolo se è la prima domanda di quel dominio).

**Righe 29–32**

```java
29          return new ExamResult(s.getTitle(), s.getMode(), LocalDateTime.now(), correct, s.size(),
30              s.getElapsedSeconds(), byDomain, s);
31      }
32  }
```

Crea il risultato con data e ora attuali (`LocalDateTime.now()`), i conteggi, i secondi impiegati e la sessione (per la revisione).
