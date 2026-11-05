# Riga per riga — pacchetto `ui` (2/3): le schede del menu principale

Questa parte copre le quattro schede della home (`ExamsPanel`, `HistoryPanel`, `ReferencePanel`, `SubnetCalculatorPanel`) e la finestra `CustomExamDialog`. Descrizione generale: [06 - ui](../06-classi-ui.md).

**File in questo documento:** [`ExamsPanel.java`](#examspaneljava) · [`CustomExamDialog.java`](#customexamdialogjava) · [`HistoryPanel.java`](#historypaneljava) · [`ReferencePanel.java`](#referencepaneljava) · [`SubnetCalculatorPanel.java`](#subnetcalculatorpaneljava)

---

## ExamsPanel.java

`src/main/java/com/ccnaexamlab/ui/ExamsPanel.java` — la scheda "Esami".

**Righe 1–31**

```java
 1  package com.ccnaexamlab.ui;
 2  
 3  import com.ccnaexamlab.app.AppContext;
 4  import com.ccnaexamlab.data.QuestionStat;
 5  import com.ccnaexamlab.engine.ExamConfig;
 6  import com.ccnaexamlab.engine.ExamFactory;
 7  import com.ccnaexamlab.engine.PresetExam;
 8  import com.ccnaexamlab.engine.QuestionFilter;
 9  import com.ccnaexamlab.model.Domain;
10  import com.ccnaexamlab.model.ExamMode;
11  import com.ccnaexamlab.model.ExamResult;
12  import com.ccnaexamlab.model.ExamSession;
13  import com.ccnaexamlab.model.Question;
14  
15  import javax.swing.BorderFactory;
16  import javax.swing.JButton;
17  import javax.swing.JCheckBox;
18  import javax.swing.JOptionPane;
19  import javax.swing.JPanel;
20  import javax.swing.JScrollPane;
21  import javax.swing.JSplitPane;
22  import javax.swing.JTable;
23  import javax.swing.ListSelectionModel;
24  import javax.swing.table.AbstractTableModel;
25  import java.awt.BorderLayout;
26  import java.awt.Color;
27  import java.awt.FlowLayout;
28  import java.awt.GridBagLayout;
29  import java.util.EnumSet;
30  import java.util.List;
31  import java.util.function.Consumer;
```

Pacchetto e import: servizi, motore d'esame, modello e componenti Swing.

**Righe 33–35**

```java
33  /** Scheda "Esami": esami predefiniti e modalità alternative. */
34  public final class ExamsPanel extends JPanel {
35      private static final long serialVersionUID = 1L;
```

Inizio della classe (`extends JPanel`).

**Righe 37–44**

```java
37      private final AppContext ctx;
38      private final Consumer<ExamSession> starter;
39      private final List<PresetExam> presets;
40      private final PresetModel presetModel = new PresetModel();
41      private final DomainModel domainModel = new DomainModel();
42      private final JTable presetTable;
43      private final JCheckBox allowBack = new JCheckBox(
44          "Consenti di tornare alle domande precedenti (l'esame Cisco reale NON lo consente)", true);
```

Campi: i servizi; `starter`, la funzione ricevuta dalla home per avviare un esame (`Consumer<ExamSession>` = "qualcosa che accetta una sessione"); la lista degli esami predefiniti; i due modelli delle tabelle; la tabella dei preset; la casella "tornare indietro" (selezionata di default).

**Righe 46–50**

```java
46      public ExamsPanel(AppContext ctx, Consumer<ExamSession> starter) {
47          super(new BorderLayout());
48          this.ctx = ctx;
49          this.starter = starter;
50          this.presets = ctx.getFactory().presetExams();
```

Costruttore: salva i parametri e calcola **una volta** la suddivisione negli esami predefiniti.

**Righe 52–60**

```java
52          // --- esami predefiniti
53          presetTable = new JTable(presetModel);
54          presetTable.setRowHeight(28);
55          presetTable.setFont(Ui.BASE);
56          presetTable.getTableHeader().setFont(Ui.BOLD.deriveFont(13f));
57          presetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
58          if (!presets.isEmpty()) {
59              presetTable.setRowSelectionInterval(0, 0);
60          }
```

Tabella dei preset: righe alte 28 pixel, carattere base, intestazione in grassetto, selezione di una sola riga; la prima riga viene selezionata subito se esiste.

**Righe 61–66**

```java
61          JPanel presetPanel = new JPanel(new BorderLayout(0, 6));
62          presetPanel.setBorder(Ui.titled("Esami predefiniti"));
63          presetPanel.add(new WrapLabel("Ogni esame contiene un set fisso di domande distribuite secondo i pesi "
64              + "del blueprint ufficiale. Svolgilo prima in Studio, poi in Simulazione per misurarti.", Ui.SMALL),
65              BorderLayout.NORTH);
66          presetPanel.add(new JScrollPane(presetTable), BorderLayout.CENTER);
```

Riquadro "Esami predefiniti": descrizione in alto, tabella al centro (dentro una `JScrollPane` per lo scorrimento).

**Righe 67–71**

```java
67          JPanel presetButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
68          JButton sim = Ui.button("▶ Avvia in Simulazione", () -> startPreset(ExamMode.SIMULATION));
69          sim.setForeground(Ui.ACCENT);
70          presetButtons.add(sim);
71          presetButtons.add(Ui.button("Avvia in Studio", () -> startPreset(ExamMode.STUDY)));
```

I due pulsanti di avvio (Simulazione in blu e Studio), ciascuno chiama `startPreset` con la modalità giusta.

**Righe 72–76**

```java
72          JPanel southPreset = new JPanel(new BorderLayout());
73          southPreset.add(presetButtons, BorderLayout.NORTH);
74          allowBack.setFont(Ui.SMALL);
75          southPreset.add(allowBack, BorderLayout.SOUTH);
76          presetPanel.add(southPreset, BorderLayout.SOUTH);
```

Parte bassa del riquadro: pulsanti sopra, casella "tornare indietro" sotto.

**Righe 78–96**

```java
78          // --- altre modalità
79          JPanel other = new JPanel(new GridBagLayout());
80          other.setBorder(Ui.titled("Altre modalità"));
81          int y = 0;
82          y = addMode(other, y, "Esame completo casuale",
83              "Domande scelte a caso da tutta la banca con i pesi ufficiali dei domini, a tempo.",
84              this::startFullRandom);
85          y = addMode(other, y, "Esame personalizzato…",
86              "Scegli domini, numero di domande, filtri (mai viste, sbagliate, marcate), modalità e tempo.",
87              this::startCustom);
88          y = addMode(other, y, "Drill di subnetting",
89              "20 esercizi di subnetting IPv4 generati al momento, con spiegazione passo-passo.",
90              this::startSubnetDrill);
91          y = addMode(other, y, "Ripassa gli errori",
92              "Studio sulle domande che hai sbagliato l'ultima volta che le hai viste.",
93              () -> startFiltered(QuestionFilter.LAST_WRONG, "Ripasso errori"));
94          y = addMode(other, y, "Ripassa le marcate",
95              "Studio sulle domande che hai marcato durante gli esami.",
96              () -> startFiltered(QuestionFilter.FLAGGED, "Ripasso domande marcate"));
```

Riquadro "Altre modalità": un pannello GridBag in cui `addMode` impila cinque righe pulsante + descrizione. Ogni chiamata restituisce la riga successiva libera. `() -> startFiltered(...)` crea al volo l'azione con i parametri giusti.

**Righe 98–105**

```java
 98          JTable domains = new JTable(domainModel);
 99          domains.setRowHeight(22);
100          domains.setFont(Ui.SMALL);
101          domains.setEnabled(false);
102          domains.getColumnModel().getColumn(0).setPreferredWidth(230);
103          for (int i = 1; i < 5; i++) {
104              domains.getColumnModel().getColumn(i).setPreferredWidth(70);
105          }
```

Tabella "Copertura e accuratezza per dominio": sola lettura (`setEnabled(false)`), carattere piccolo, prima colonna più larga.

**Righe 106–113**

```java
106          JScrollPane dsp = new JScrollPane(domains);
107          dsp.setPreferredSize(new java.awt.Dimension(300, 175));
108          JPanel dp = new JPanel(new BorderLayout());
109          dp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
110          dp.add(Ui.label("Copertura e accuratezza per dominio", Ui.BOLD, Ui.HEADER_BG), BorderLayout.NORTH);
111          dp.add(dsp, BorderLayout.CENTER);
112          other.add(dp, Ui.row(y++));
113          Ui.addFiller(other, y);
```

La tabella dentro una barra di scorrimento di altezza fissa, preceduta da un titolo, aggiunta al riquadro; poi il riempitivo che spinge tutto in alto.

**Righe 115–119**

```java
115          JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, presetPanel, new JScrollPane(other));
116          split.setResizeWeight(0.55);
117          split.setBorder(Ui.padding(8, 8));
118          add(split, BorderLayout.CENTER);
119      }
```

Divide lo spazio in due colonne ridimensionabili trascinando il separatore (55% a sinistra).

**Righe 121–133**

```java
121      private int addMode(JPanel p, int y, String title, String desc, Runnable action) {
122          JPanel row = new JPanel(new BorderLayout(10, 0));
123          row.setOpaque(false);
124          row.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
125          JButton b = Ui.button(title, action);
126          b.setPreferredSize(new java.awt.Dimension(230, 34));
127          row.add(b, BorderLayout.WEST);
128          WrapLabel l = new WrapLabel(desc, Ui.SMALL);
129          l.setForeground(new Color(0x445566));
130          row.add(l, BorderLayout.CENTER);
131          p.add(row, Ui.row(y));
132          return y + 1;
133      }
```

Crea una riga con un pulsante di larghezza fissa (230×34) a sinistra e la descrizione in grigio che va a capo al centro; la aggiunge alla riga `y` e restituisce `y + 1`.

**Righe 135–138**

```java
135      public void refresh() {
136          presetModel.fireTableDataChanged();
137          domainModel.fireTableDataChanged();
138      }
```

Chiede alle due tabelle di rileggere i dati (dopo un esame cambiano tentativi, punteggi e accuratezza).

**Riga 140**

```java
140      // ------------------------------------------------------------------ azioni
```

Separatore: le azioni dei pulsanti.

**Righe 142–160**

```java
142      private void startPreset(ExamMode mode) {
143          int row = presetTable.getSelectedRow();
144          if (row < 0) {
145              JOptionPane.showMessageDialog(this, "Seleziona un esame dalla tabella.");
146              return;
147          }
148          PresetExam p = presets.get(row);
149          boolean back = allowBack.isSelected() || mode == ExamMode.STUDY;
150          if (mode == ExamMode.SIMULATION) {
151              int c = JOptionPane.showConfirmDialog(this,
152                  p.getName() + ": " + p.size() + " domande in " + ExamFactory.suggestedMinutes(p.size())
153                      + " minuti.\nIl cronometro parte subito. Pronto?",
154                  "Avvio simulazione", JOptionPane.OK_CANCEL_OPTION);
155              if (c != JOptionPane.OK_OPTION) {
156                  return;
157              }
158          }
159          starter.accept(ctx.getFactory().fromPreset(p, mode, back));
160      }
```

Avvia l'esame selezionato. Se nessuna riga è selezionata avvisa. In Studio il ritorno indietro è sempre consentito; in Simulazione dipende dalla casella e prima chiede conferma mostrando domande e minuti. Infine chiede alla fabbrica la sessione e la passa a `starter`.

**Righe 162–171**

```java
162      private void startFullRandom() {
163          int n = Math.min(100, ctx.getBank().size());
164          ExamConfig cfg = new ExamConfig()
165              .title("Esame completo casuale")
166              .mode(ExamMode.SIMULATION)
167              .questionCount(n)
168              .timeMinutes(ExamFactory.suggestedMinutes(n))
169              .allowBack(allowBack.isSelected());
170          starter.accept(ctx.getFactory().custom(cfg));
171      }
```

Esame completo casuale: 100 domande (o tutte se la banca è più piccola), Simulazione, minuti suggeriti, regola "indietro" dalla casella.

**Righe 173–187**

```java
173      private void startCustom() {
174          CustomExamDialog d = new CustomExamDialog(javax.swing.SwingUtilities.getWindowAncestor(this));
175          d.setVisible(true);
176          ExamConfig cfg = d.getResult();
177          if (cfg == null) {
178              return;
179          }
180          ExamSession s = ctx.getFactory().custom(cfg);
181          if (s == null) {
182              JOptionPane.showMessageDialog(this, "Nessuna domanda corrisponde ai criteri scelti.",
183                  "Esame vuoto", JOptionPane.INFORMATION_MESSAGE);
184              return;
185          }
186          starter.accept(s);
187      }
```

Apre la finestra di configurazione (modale: il codice si ferma finché non viene chiusa), legge la configurazione (null = annullato), crea la sessione e, se è vuota, avvisa invece di avviare.

**Righe 189–197**

```java
189      private void startSubnetDrill() {
190          ExamConfig cfg = new ExamConfig()
191              .title("Drill di subnetting")
192              .mode(ExamMode.STUDY)
193              .domains(EnumSet.noneOf(Domain.class))
194              .questionCount(0)
195              .subnetQuestions(20);
196          starter.accept(ctx.getFactory().custom(cfg));
197      }
```

Drill: nessun dominio della banca, 0 domande normali, 20 domande di subnetting generate, modalità Studio.

**Righe 199–209**

```java
199      private void startFiltered(QuestionFilter f, String title) {
200          ExamConfig cfg = new ExamConfig().title(title).mode(ExamMode.STUDY).filter(f)
201              .questionCount(Integer.MAX_VALUE).weighted(false);
202          ExamSession s = ctx.getFactory().custom(cfg);
203          if (s == null) {
204              JOptionPane.showMessageDialog(this, "Non ci sono domande da ripassare per ora.\n"
205                  + "Svolgi qualche esame: errori e domande marcate verranno raccolti qui.");
206              return;
207          }
208          starter.accept(s);
209      }
```

Ripasso: tutte le domande che passano il filtro (`Integer.MAX_VALUE` = "nessun limite"), senza pesi, in Studio. Se non ce ne sono, spiega che verranno raccolte svolgendo esami.

**Riga 211**

```java
211      // ------------------------------------------------------------------ modelli tabelle
```

Separatore: i modelli delle due tabelle.

**Righe 213–221**

```java
213      private final class PresetModel extends AbstractTableModel {
214          private static final long serialVersionUID = 1L;
215          private final String[] cols = {"Esame", "Domande", "Tempo", "Tentativi", "Miglior punteggio", "Ultimo"};
216  
217          @Override public int getRowCount() { return presets.size(); }
218          @Override public int getColumnCount() { return cols.length; }
219          @Override public String getColumnName(int c) { return cols[c]; }
220  
221          @Override
```

Modello della tabella dei preset (classe interna: può usare i campi di `ExamsPanel`). L'array `cols` contiene i nomi delle colonne; i tre metodi scritti su una riga dicono alla tabella quante righe ci sono (una per preset), quante colonne e come si chiamano. `@Override` indica che ridefiniscono metodi di `AbstractTableModel`. L'ultimo `@Override` introduce `getValueAt(r, c)`, che la tabella chiama per ogni cella da disegnare.

**Righe 222–243**

```java
222          public Object getValueAt(int r, int c) {
223              PresetExam p = presets.get(r);
224              int attempts = 0;
225              int best = -1;
226              int last = -1;
227              for (ExamResult res : ctx.getStore().getHistory()) {
228                  if (res.getTitle().equals(p.getName())) {
229                      attempts++;
230                      best = Math.max(best, res.getScaledScore());
231                      last = res.getScaledScore();
232                  }
233              }
234              switch (c) {
235                  case 0: return p.getName();
236                  case 1: return p.size();
237                  case 2: return ExamFactory.suggestedMinutes(p.size()) + " min";
238                  case 3: return attempts;
239                  case 4: return best < 0 ? "—" : best + (best >= ExamResult.PASSING_SCORE ? " ✔" : "");
240                  default: return last < 0 ? "—" : String.valueOf(last);
241              }
242          }
243      }
```

Per il preset della riga scorre lo storico cercando i risultati con lo stesso titolo: conta i tentativi, tiene il punteggio migliore (`Math.max`) e l'ultimo. Poi restituisce il valore della colonna richiesta: nome, domande, minuti, tentativi, migliore (con ✔ se ≥ 825, trattino se nessuno), ultimo.

**Righe 245–253**

```java
245      private final class DomainModel extends AbstractTableModel {
246          private static final long serialVersionUID = 1L;
247          private final String[] cols = {"Dominio", "Peso", "Domande", "Viste", "Accuratezza"};
248  
249          @Override public int getRowCount() { return Domain.values().length; }
250          @Override public int getColumnCount() { return cols.length; }
251          @Override public String getColumnName(int c) { return cols[c]; }
252  
253          @Override
```

Modello della tabella dei domini: una riga per dominio, colonne e nomi come sopra; segue `getValueAt`.

**Righe 254–277**

```java
254          public Object getValueAt(int r, int c) {
255              Domain d = Domain.values()[r];
256              List<Question> qs = ctx.getBank().byDomain(d);
257              int seenQuestions = 0;
258              int answers = 0;
259              int correct = 0;
260              for (Question q : qs) {
261                  QuestionStat s = ctx.getStore().peekStat(q.getId());
262                  if (s != null && s.getSeen() > 0) {
263                      seenQuestions++;
264                      answers += s.getSeen();
265                      correct += s.getCorrect();
266                  }
267              }
268              switch (c) {
269                  case 0: return d.getLabel();
270                  case 1: return d.getWeight() + "%";
271                  case 2: return qs.size();
272                  case 3: return seenQuestions;
273                  default: return answers == 0 ? "—" : Ui.pct(100.0 * correct / answers);
274              }
275          }
276      }
277  }
```

Per il dominio della riga scorre le sue domande e somma: quante sono state viste almeno una volta, quante risposte totali e quante corrette. Restituisce nome, peso, numero di domande, viste e accuratezza (o trattino se mai risposto).

---

## CustomExamDialog.java

`src/main/java/com/ccnaexamlab/ui/CustomExamDialog.java` — la finestra "Esame personalizzato".

**Righe 1–28**

```java
 1  package com.ccnaexamlab.ui;
 2  
 3  import com.ccnaexamlab.engine.ExamConfig;
 4  import com.ccnaexamlab.engine.ExamFactory;
 5  import com.ccnaexamlab.engine.QuestionFilter;
 6  import com.ccnaexamlab.model.Domain;
 7  import com.ccnaexamlab.model.ExamMode;
 8  
 9  import javax.swing.JCheckBox;
10  import javax.swing.JComboBox;
11  import javax.swing.JDialog;
12  import javax.swing.JLabel;
13  import javax.swing.JOptionPane;
14  import javax.swing.JPanel;
15  import javax.swing.JSpinner;
16  import javax.swing.JTextField;
17  import javax.swing.SpinnerNumberModel;
18  import java.awt.BorderLayout;
19  import java.awt.FlowLayout;
20  import java.awt.GridBagConstraints;
21  import java.awt.GridBagLayout;
22  import java.awt.GridLayout;
23  import java.awt.Insets;
24  import java.awt.Window;
25  import java.util.EnumMap;
26  import java.util.EnumSet;
27  import java.util.Map;
28  import java.util.Set;
```

Pacchetto e import.

**Righe 30–32**

```java
30  /** Finestra di configurazione di un esame personalizzato. */
31  public final class CustomExamDialog extends JDialog {
32      private static final long serialVersionUID = 1L;
```

`extends JDialog`: finestra secondaria.

**Righe 34–43**

```java
34      private final Map<Domain, JCheckBox> domainBoxes = new EnumMap<>(Domain.class);
35      private final JTextField title = new JTextField("Esame personalizzato", 24);
36      private final JComboBox<ExamMode> mode = new JComboBox<>(ExamMode.values());
37      private final JComboBox<QuestionFilter> filter = new JComboBox<>(QuestionFilter.values());
38      private final JSpinner count = new JSpinner(new SpinnerNumberModel(30, 1, 500, 5));
39      private final JSpinner subnet = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
40      private final JSpinner minutes = new JSpinner(new SpinnerNumberModel(36, 0, 600, 5));
41      private final JCheckBox weighted = new JCheckBox("Distribuisci secondo i pesi del blueprint", true);
42      private final JCheckBox allowBack = new JCheckBox("Consenti di tornare indietro", true);
43      private ExamConfig result;
```

I controlli della finestra: una casella per dominio, campo titolo, menu modalità e filtro, tre **spinner** (campi numerici con frecce; i numeri sono valore iniziale, minimo, massimo, passo), le caselle "pesi" e "indietro", e il risultato (null finché non si conferma).

**Righe 45–46**

```java
45      public CustomExamDialog(Window owner) {
46          super(owner, "Esame personalizzato", ModalityType.APPLICATION_MODAL);
```

Costruttore: crea una finestra **modale** (`APPLICATION_MODAL`) legata alla finestra principale.

**Righe 48–58**

```java
48          JPanel domains = new JPanel(new GridLayout(0, 1));
49          domains.setBorder(Ui.titled("Domini"));
50          for (Domain d : Domain.values()) {
51              JCheckBox cb = new JCheckBox(d.getLabel() + " (" + d.getWeight() + "%)", true);
52              domainBoxes.put(d, cb);
53              domains.add(cb);
54          }
55          JPanel selButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
56          selButtons.add(Ui.button("Tutti", () -> domainBoxes.values().forEach(b -> b.setSelected(true))));
57          selButtons.add(Ui.button("Nessuno", () -> domainBoxes.values().forEach(b -> b.setSelected(false))));
58          domains.add(selButtons);
```

Colonna dei domini: una casella selezionata per ognuno (con il peso), salvata nella mappa; sotto, i pulsanti "Tutti" e "Nessuno" che selezionano/deselezionano tutte le caselle.

**Righe 60–68**

```java
60          JPanel opts = new JPanel(new GridBagLayout());
61          opts.setBorder(Ui.titled("Opzioni"));
62          int y = 0;
63          y = addField(opts, y, "Titolo:", title);
64          y = addField(opts, y, "Modalità:", mode);
65          y = addField(opts, y, "Filtro domande:", filter);
66          y = addField(opts, y, "Numero di domande:", count);
67          y = addField(opts, y, "Domande di subnetting generate:", subnet);
68          y = addField(opts, y, "Tempo (minuti, 0 = illimitato):", minutes);
```

Riquadro opzioni: `addField` aggiunge coppie etichetta + controllo, una per riga.

**Righe 69–76**

```java
69          GridBagConstraints c = new GridBagConstraints();
70          c.gridx = 0;
71          c.gridy = y++;
72          c.gridwidth = 2;
73          c.anchor = GridBagConstraints.WEST;
74          opts.add(weighted, c);
75          c.gridy = y;
76          opts.add(allowBack, c);
```

Le due caselle finali occupano entrambe le colonne (`gridwidth = 2`), allineate a sinistra.

**Righe 78–81**

```java
78          count.addChangeListener(e -> updateMinutes());
79          subnet.addChangeListener(e -> updateMinutes());
80          mode.addActionListener(e -> updateMinutes());
81          mode.setSelectedItem(ExamMode.STUDY);
```

Quando cambiano numero di domande, subnetting o modalità, ricalcola i minuti. Poi imposta la modalità iniziale a Studio (che a sua volta azzera i minuti).

**Righe 83–86**

```java
83          JPanel center = new JPanel(new BorderLayout(8, 8));
84          center.setBorder(Ui.padding(10, 10));
85          center.add(domains, BorderLayout.WEST);
86          center.add(opts, BorderLayout.CENTER);
```

Composizione: domini a sinistra, opzioni al centro.

**Righe 88–90**

```java
88          JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
89          buttons.add(Ui.button("Annulla", this::dispose));
90          buttons.add(Ui.button("Crea esame", this::confirm));
```

Pulsanti "Annulla" (chiude senza risultato) e "Crea esame" (`confirm`).

**Righe 92–100**

```java
 92          JPanel root = new JPanel(new BorderLayout());
 93          root.add(Ui.header("Esame personalizzato", "Allena i punti deboli: scegli domini e filtri"),
 94              BorderLayout.NORTH);
 95          root.add(center, BorderLayout.CENTER);
 96          root.add(buttons, BorderLayout.SOUTH);
 97          setContentPane(root);
 98          pack();
 99          setLocationRelativeTo(owner);
100      }
```

Intestazione, contenuto e pulsanti; `pack()` dimensiona la finestra sul contenuto, poi la centra sopra la finestra principale.

**Righe 102–114**

```java
102      private static int addField(JPanel p, int y, String label, java.awt.Component field) {
103          GridBagConstraints c = new GridBagConstraints();
104          c.gridx = 0;
105          c.gridy = y;
106          c.anchor = GridBagConstraints.WEST;
107          c.insets = new Insets(4, 4, 4, 8);
108          p.add(new JLabel(label), c);
109          c.gridx = 1;
110          c.fill = GridBagConstraints.HORIZONTAL;
111          c.weightx = 1;
112          p.add(field, c);
113          return y + 1;
114      }
```

Aggiunge l'etichetta nella colonna 0 e il controllo nella colonna 1 (che si allarga), con un po' di margine; restituisce la riga successiva.

**Righe 116–123**

```java
116      private void updateMinutes() {
117          if (mode.getSelectedItem() == ExamMode.STUDY) {
118              minutes.setValue(0);
119          } else {
120              int n = (Integer) count.getValue() + (Integer) subnet.getValue();
121              minutes.setValue(ExamFactory.suggestedMinutes(n));
122          }
123      }
```

In Studio i minuti diventano 0 (illimitato); in Simulazione minuti suggeriti per domande + subnetting. I valori degli spinner sono oggetti `Integer`, da qui il cast `(Integer)`.

**Righe 125–147**

```java
125      private void confirm() {
126          Set<Domain> sel = EnumSet.noneOf(Domain.class);
127          domainBoxes.forEach((d, b) -> {
128              if (b.isSelected()) {
129                  sel.add(d);
130              }
131          });
132          if (sel.isEmpty() && (Integer) subnet.getValue() == 0) {
133              JOptionPane.showMessageDialog(this, "Seleziona almeno un dominio o delle domande di subnetting.");
134              return;
135          }
136          result = new ExamConfig()
137              .title(title.getText().isBlank() ? "Esame personalizzato" : title.getText().trim())
138              .mode((ExamMode) mode.getSelectedItem())
139              .domains(sel)
140              .filter((QuestionFilter) filter.getSelectedItem())
141              .questionCount((Integer) count.getValue())
142              .subnetQuestions((Integer) subnet.getValue())
143              .timeMinutes((Integer) minutes.getValue())
144              .weighted(weighted.isSelected())
145              .allowBack(allowBack.isSelected());
146          dispose();
147      }
```

Raccoglie i domini selezionati; se non c'è né un dominio né del subnetting avvisa e resta aperta. Altrimenti compila un `ExamConfig` con tutti i valori (titolo vuoto → titolo di default) e chiude la finestra.

**Righe 149–151**

```java
149      /** null se l'utente ha annullato. */
150      public ExamConfig getResult() { return result; }
151  }
```

Restituisce la configurazione a chi ha aperto la finestra.

---

## HistoryPanel.java

`src/main/java/com/ccnaexamlab/ui/HistoryPanel.java` — la scheda "Storico e progressi".

**Righe 1–23**

```java
 1  package com.ccnaexamlab.ui;
 2  
 3  import com.ccnaexamlab.app.AppContext;
 4  import com.ccnaexamlab.model.Domain;
 5  import com.ccnaexamlab.model.DomainScore;
 6  import com.ccnaexamlab.model.ExamResult;
 7  import com.ccnaexamlab.util.Text;
 8  
 9  import javax.swing.JButton;
10  import javax.swing.JOptionPane;
11  import javax.swing.JPanel;
12  import javax.swing.JProgressBar;
13  import javax.swing.JScrollPane;
14  import javax.swing.JSplitPane;
15  import javax.swing.JTable;
16  import javax.swing.table.AbstractTableModel;
17  import java.awt.BorderLayout;
18  import java.awt.FlowLayout;
19  import java.awt.GridBagLayout;
20  import java.time.format.DateTimeFormatter;
21  import java.util.EnumMap;
22  import java.util.List;
23  import java.util.Map;
```

Pacchetto e import.

**Righe 25–28**

```java
25  /** Scheda "Storico e progressi": elenco esami, grafico punteggi, aggregato per dominio. */
26  public final class HistoryPanel extends JPanel {
27      private static final long serialVersionUID = 1L;
28      private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
```

Inizio della classe; `FMT` è il formato di data `05/11/2026 12:08`.

**Righe 30–33**

```java
30      private final AppContext ctx;
31      private final HistoryModel model = new HistoryModel();
32      private final ScoreChart chart = new ScoreChart();
33      private final JPanel domainPanel = new JPanel(new GridBagLayout());
```

Campi: servizi, modello della tabella, grafico e pannello dell'accuratezza per dominio.

**Righe 35–42**

```java
35      public HistoryPanel(AppContext ctx) {
36          super(new BorderLayout());
37          this.ctx = ctx;
38  
39          JTable table = new JTable(model);
40          table.setRowHeight(24);
41          table.setFont(Ui.BASE.deriveFont(14f));
42          table.setAutoCreateRowSorter(true);
```

Tabella dello storico; `setAutoCreateRowSorter(true)` permette di ordinare cliccando sulle intestazioni.

**Righe 43–45**

```java
43          JPanel tablePanel = new JPanel(new BorderLayout());
44          tablePanel.setBorder(Ui.titled("Esami svolti"));
45          tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
```

Riquadro "Esami svolti" con la tabella.

**Righe 46–50**

```java
46          JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
47          JButton clear = Ui.button("Azzera storico e statistiche", this::clear);
48          clear.setForeground(Ui.KO);
49          buttons.add(clear);
50          tablePanel.add(buttons, BorderLayout.SOUTH);
```

Pulsante rosso "Azzera storico e statistiche".

**Righe 52–65**

```java
52          JPanel chartPanel = new JPanel(new BorderLayout());
53          chartPanel.setBorder(Ui.titled("Andamento dei punteggi"));
54          chartPanel.add(chart, BorderLayout.CENTER);
55  
56          domainPanel.setBorder(Ui.titled("Accuratezza complessiva per dominio"));
57  
58          JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, new JScrollPane(domainPanel));
59          right.setResizeWeight(0.5);
60          JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, right);
61          split.setResizeWeight(0.55);
62          split.setBorder(Ui.padding(8, 8));
63          add(split, BorderLayout.CENTER);
64          refresh();
65      }
```

Riquadro del grafico e del pannello domini, divisi in verticale; poi tabella a sinistra e la colonna destra, divise in orizzontale. Alla fine un primo `refresh()` riempie tutto.

**Righe 67–75**

```java
67      public void refresh() {
68          List<ExamResult> history = ctx.getStore().getHistory();
69          model.fireTableDataChanged();
70          chart.setResults(history);
71  
72          Map<Domain, DomainScore> agg = new EnumMap<>(Domain.class);
73          for (ExamResult r : history) {
74              r.getByDomain().forEach((d, s) -> agg.computeIfAbsent(d, k -> new DomainScore()).add(s));
75          }
```

Aggiorna tabella e grafico; poi somma i punteggi per dominio di tutti gli esami (`forEach` sulla mappa di ogni risultato).

**Righe 76–95**

```java
76          domainPanel.removeAll();
77          int y = 0;
78          for (Domain d : Domain.values()) {
79              DomainScore s = agg.get(d);
80              domainPanel.add(Ui.label(d.getLabel(), Ui.BOLD.deriveFont(13f), null), Ui.row(y++));
81              JProgressBar bar = new JProgressBar(0, 100);
82              bar.setStringPainted(true);
83              if (s == null || s.getTotal() == 0) {
84                  bar.setString("nessun dato");
85              } else {
86                  bar.setValue((int) Math.round(s.getPercent()));
87                  bar.setString(s.getCorrect() + " / " + s.getTotal() + "  (" + Ui.pct(s.getPercent()) + ")");
88                  bar.setForeground(s.getPercent() >= 82.5 ? Ui.OK : s.getPercent() >= 60 ? Ui.WARN : Ui.KO);
89              }
90              domainPanel.add(bar, Ui.row(y++));
91          }
92          Ui.addFiller(domainPanel, y);
93          domainPanel.revalidate();
94          domainPanel.repaint();
95      }
```

Ricostruisce il pannello: per ogni dominio un'etichetta e una barra; senza dati la barra dice "nessun dato", altrimenti mostra corrette/totale e percentuale con il colore verde/arancio/rosso. `revalidate` e `repaint` fanno ricalcolare e ridisegnare il pannello.

**Righe 97–105**

```java
 97      private void clear() {
 98          int c = JOptionPane.showConfirmDialog(this,
 99              "Cancellare definitivamente storico esami e statistiche delle domande?", "Conferma",
100              JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
101          if (c == JOptionPane.YES_OPTION) {
102              ctx.getStore().clearHistory();
103              refresh();
104          }
105      }
```

Chiede conferma e, se accettata, cancella storico e statistiche e aggiorna la vista.

**Righe 107–115**

```java
107      private final class HistoryModel extends AbstractTableModel {
108          private static final long serialVersionUID = 1L;
109          private final String[] cols = {"Data", "Esame", "Modalità", "Corrette", "Punteggio", "Esito", "Durata"};
110  
111          @Override public int getRowCount() { return ctx.getStore().getHistory().size(); }
112          @Override public int getColumnCount() { return cols.length; }
113          @Override public String getColumnName(int c) { return cols[c]; }
114  
115          @Override
```

Modello della tabella: colonne e nomi, numero di righe = numero di esami.

**Righe 116–118**

```java
116          public Class<?> getColumnClass(int c) { return c == 4 ? Integer.class : String.class; }
117  
118          @Override
```

`getColumnClass`: la colonna Punteggio è dichiarata numerica, così l'ordinamento è numerico (900 dopo 850) e non alfabetico. Il successivo `@Override` introduce `getValueAt`.

**Righe 119–133**

```java
119          public Object getValueAt(int r, int c) {
120              List<ExamResult> h = ctx.getStore().getHistory();
121              ExamResult res = h.get(h.size() - 1 - r); // più recenti in alto
122              switch (c) {
123                  case 0: return res.getDate().format(FMT);
124                  case 1: return res.getTitle();
125                  case 2: return res.getMode().getLabel();
126                  case 3: return res.getCorrect() + "/" + res.getTotal();
127                  case 4: return res.getScaledScore();
128                  case 5: return res.isPassed() ? "✔ Superato" : "✘ Non superato";
129                  default: return Text.formatDuration(res.getDurationSeconds());
130              }
131          }
132      }
133  }
```

Legge lo storico al contrario (`h.size() - 1 - r`) per mostrare i più recenti in alto, e restituisce data formattata, titolo, modalità, corrette/totale, punteggio, esito, durata.

---

## ReferencePanel.java

`src/main/java/com/ccnaexamlab/ui/ReferencePanel.java` — la scheda "Cheat sheet".

**Righe 1–21**

```java
 1  package com.ccnaexamlab.ui;
 2  
 3  import javax.swing.DefaultListModel;
 4  import javax.swing.JEditorPane;
 5  import javax.swing.JList;
 6  import javax.swing.JScrollPane;
 7  import javax.swing.JSplitPane;
 8  import javax.swing.JPanel;
 9  import javax.swing.ListSelectionModel;
10  import javax.swing.SwingUtilities;
11  import javax.swing.text.html.HTMLEditorKit;
12  import javax.swing.text.html.StyleSheet;
13  import java.awt.BorderLayout;
14  import java.io.BufferedReader;
15  import java.io.IOException;
16  import java.io.InputStream;
17  import java.io.InputStreamReader;
18  import java.nio.charset.StandardCharsets;
19  import java.util.ArrayList;
20  import java.util.List;
21  import java.util.stream.Collectors;
```

Pacchetto e import (lista, visualizzatore HTML, lettura delle risorse).

**Righe 23–25**

```java
23  /** Scheda "Cheat sheet": schede di ripasso in HTML caricate dalle risorse. */
24  public final class ReferencePanel extends JPanel {
25      private static final long serialVersionUID = 1L;
```

Inizio della classe.

**Righe 27–28**

```java
27      private final List<String> files = new ArrayList<>();
28      private final JEditorPane pane = new JEditorPane();
```

Nomi dei file HTML (stessa posizione dei titoli nella lista) e il visualizzatore.

**Righe 30–33**

```java
30      public ReferencePanel() {
31          super(new BorderLayout());
32          DefaultListModel<String> titles = new DefaultListModel<>();
33          loadIndex(titles);
```

Costruttore: carica l'indice nei titoli.

**Righe 35–43**

```java
35          JList<String> list = new JList<>(titles);
36          list.setFont(Ui.BASE);
37          list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
38          list.setFixedCellHeight(30);
39          list.addListSelectionListener(e -> {
40              if (!e.getValueIsAdjusting() && list.getSelectedIndex() >= 0) {
41                  show(files.get(list.getSelectedIndex()));
42              }
43          });
```

Lista dei titoli: quando la selezione cambia (e non è ancora "in movimento", `getValueIsAdjusting`), mostra il file corrispondente.

**Righe 45–55**

```java
45          HTMLEditorKit kit = new HTMLEditorKit();
46          StyleSheet css = kit.getStyleSheet();
47          css.addRule("body { font-family: sans-serif; font-size: 13pt; margin: 12px; color: #1b1b1b; }");
48          css.addRule("h1 { color: #1F3A5F; font-size: 20pt; }");
49          css.addRule("h2 { color: #2E6DB4; font-size: 15pt; margin-top: 14px; }");
50          css.addRule("th { background-color: #1F3A5F; color: white; padding: 4px; text-align: left; }");
51          css.addRule("td { padding: 4px; border-bottom: 1px solid #dddddd; }");
52          css.addRule("code, pre { font-family: monospace; font-size: 12pt; background-color: #f1f3f6; }");
53          css.addRule(".tip { background-color: #fff6dc; padding: 6px; border-left: 4px solid #e69500; }");
54          pane.setEditorKit(kit);
55          pane.setEditable(false);
```

Il motore HTML di Swing con le regole di stile CSS del programma: carattere, colori dei titoli, intestazioni di tabella blu, righe separate, codice su sfondo grigio, riquadro `.tip` giallo con bordo arancio. Il visualizzatore non è modificabile.

**Righe 57–64**

```java
57          JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), new JScrollPane(pane));
58          split.setDividerLocation(260);
59          split.setBorder(Ui.padding(8, 8));
60          add(split, BorderLayout.CENTER);
61          if (!files.isEmpty()) {
62              list.setSelectedIndex(0);
63          }
64      }
```

Lista a sinistra (260 pixel) e pagina a destra; seleziona subito la prima pagina.

**Righe 66–84**

```java
66      private void loadIndex(DefaultListModel<String> titles) {
67          InputStream in = getClass().getResourceAsStream("/reference/index.txt");
68          if (in == null) {
69              return;
70          }
71          try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
72              String line;
73              while ((line = br.readLine()) != null) {
74                  int sep = line.indexOf('|');
75                  if (line.isBlank() || line.startsWith("#") || sep < 0) {
76                      continue;
77                  }
78                  files.add(line.substring(0, sep).trim());
79                  titles.addElement(line.substring(sep + 1).trim());
80              }
81          } catch (IOException e) {
82              titles.addElement("Errore: " + e.getMessage());
83          }
84      }
```

Legge `/reference/index.txt`: per ogni riga valida divide su `|` il nome del file e il titolo; righe vuote, commenti o senza `|` vengono saltate. Un errore di lettura viene mostrato come voce della lista.

**Righe 86–99**

```java
86      private void show(String file) {
87          InputStream in = getClass().getResourceAsStream("/reference/" + file);
88          if (in == null) {
89              pane.setText("<html><body>Risorsa non trovata: " + file + "</body></html>");
90              return;
91          }
92          try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
93              pane.setText(br.lines().collect(Collectors.joining("\n")));
94          } catch (IOException e) {
95              pane.setText("<html><body>Errore: " + e.getMessage() + "</body></html>");
96          }
97          SwingUtilities.invokeLater(() -> pane.setCaretPosition(0));
98      }
99  }
```

Apre la pagina come risorsa e la carica nel visualizzatore (`lines().collect(joining)` unisce tutte le righe); se manca o non si legge, mostra un messaggio. Infine riporta lo scorrimento in cima (`setCaretPosition(0)`).

---

## SubnetCalculatorPanel.java

`src/main/java/com/ccnaexamlab/ui/SubnetCalculatorPanel.java` — la scheda "Calcolatore subnet".

**Righe 1–12**

```java
 1  package com.ccnaexamlab.ui;
 2  
 3  import com.ccnaexamlab.util.IpUtils;
 4  
 5  import javax.swing.JLabel;
 6  import javax.swing.JPanel;
 7  import javax.swing.JTable;
 8  import javax.swing.JTextField;
 9  import javax.swing.JScrollPane;
10  import javax.swing.table.DefaultTableModel;
11  import java.awt.BorderLayout;
12  import java.awt.FlowLayout;
```

Pacchetto e import.

**Righe 14–16**

```java
14  /** Calcolatore IPv4: rete, broadcast, range host, maschera, wildcard, binario. */
15  public final class SubnetCalculatorPanel extends JPanel {
16      private static final long serialVersionUID = 1L;
```

Inizio della classe.

**Righe 18–24**

```java
18      private final JTextField input = new JTextField("192.168.10.77/26", 22);
19      private final DefaultTableModel model = new DefaultTableModel(new Object[] {"Campo", "Valore"}, 0) {
20          private static final long serialVersionUID = 1L;
21          @Override
22          public boolean isCellEditable(int r, int c) { return false; }
23      };
24      private final JLabel error = Ui.label(" ", Ui.BOLD, Ui.KO);
```

Campo di input con un esempio precompilato; modello della tabella a due colonne in cui nessuna cella è modificabile (classe anonima che ridefinisce `isCellEditable`); etichetta rossa per gli errori.

**Righe 26–34**

```java
26      public SubnetCalculatorPanel() {
27          super(new BorderLayout());
28          JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
29          top.add(Ui.label("Indirizzo (es. 10.1.2.3/20 oppure 10.1.2.3 255.255.240.0):", Ui.BOLD, null));
30          input.setFont(Ui.MONO.deriveFont(16f));
31          input.addActionListener(e -> calculate());
32          top.add(input);
33          top.add(Ui.button("Calcola", this::calculate));
34          top.add(error);
```

Riga superiore: istruzioni, campo (premere Invio esegue il calcolo), pulsante "Calcola" ed etichetta degli errori.

**Righe 36–40**

```java
36          JTable table = new JTable(model);
37          table.setRowHeight(26);
38          table.setFont(Ui.MONO.deriveFont(15f));
39          table.getColumnModel().getColumn(0).setMinWidth(230);
40          table.getColumnModel().getColumn(0).setMaxWidth(260);
```

Tabella con carattere monospazio e prima colonna di larghezza fissa.

**Righe 42–52**

```java
42          JPanel center = new JPanel(new BorderLayout());
43          center.setBorder(Ui.padding(4, 8));
44          center.add(new JScrollPane(table), BorderLayout.CENTER);
45          center.add(new WrapLabel("Suggerimento: all'esame non hai calcolatrici. Usa questo strumento solo per "
46              + "verificare i calcoli fatti a mano con il metodo del magic number (256 - valore della maschera "
47              + "nell'ottetto interessante).", Ui.SMALL), BorderLayout.SOUTH);
48  
49          add(top, BorderLayout.NORTH);
50          add(center, BorderLayout.CENTER);
51          calculate();
52      }
```

Tabella al centro e un suggerimento sotto; poi composizione del pannello ed esecuzione di un primo calcolo sull'esempio.

**Righe 54–72**

```java
54      private void calculate() {
55          model.setRowCount(0);
56          error.setText(" ");
57          try {
58              String s = input.getText().trim();
59              String ipPart;
60              int prefix;
61              if (s.contains("/")) {
62                  String[] p = s.split("/");
63                  ipPart = p[0];
64                  prefix = Integer.parseInt(p[1].trim());
65              } else {
66                  String[] p = s.split("\\s+");
67                  if (p.length != 2) {
68                      throw new IllegalArgumentException("Specifica prefisso (/24) o maschera");
69                  }
70                  ipPart = p[0];
71                  prefix = IpUtils.prefixFromMask(p[1]);
72              }
```

Svuota la tabella e l'errore. Se l'input contiene `/` lo divide in IP e prefisso; altrimenti si aspetta "IP maschera" separati da spazi e converte la maschera in prefisso.

**Righe 73–97**

```java
73              long ip = IpUtils.toLong(ipPart);
74              long net = IpUtils.network(ip, prefix);
75              long bc = IpUtils.broadcast(ip, prefix);
76              add("Indirizzo", IpUtils.toIp(ip));
77              add("Prefisso / maschera", "/" + prefix + "  =  " + IpUtils.maskString(prefix));
78              add("Wildcard mask", IpUtils.wildcardString(prefix));
79              add("Indirizzo di rete", IpUtils.toIp(net));
80              add("Broadcast", IpUtils.toIp(bc));
81              if (prefix <= 30) {
82                  add("Primo host", IpUtils.toIp(net + 1));
83                  add("Ultimo host", IpUtils.toIp(bc - 1));
84              } else if (prefix == 31) {
85                  add("Host (RFC 3021)", IpUtils.toIp(net) + " – " + IpUtils.toIp(bc));
86              }
87              add("Host utilizzabili", String.valueOf(IpUtils.usableHosts(prefix)));
88              add("Indirizzi totali", String.valueOf(1L << (32 - prefix)));
89              if (prefix > 0 && prefix % 8 != 0) {
90                  int oct = (prefix - 1) / 8;
91                  int maskOct = (int) ((IpUtils.mask(prefix) >> (24 - 8 * oct)) & 255);
92                  add("Magic number", (256 - maskOct) + " nel " + (oct + 1) + "° ottetto");
93              }
94              add("Classe (classful)", String.valueOf(IpUtils.ipClass(ip)));
95              add("Privato RFC 1918", IpUtils.isPrivate(ip) ? "sì" : "no");
96              add("Binario indirizzo", IpUtils.toBinary(ip));
97              add("Binario maschera", IpUtils.toBinary(IpUtils.mask(prefix)));
```

Calcola e aggiunge le righe: indirizzo, prefisso e maschera, wildcard, rete, broadcast; primo e ultimo host (per /31 un'indicazione speciale, per /32 nessuna); host utilizzabili e indirizzi totali; magic number solo se il prefisso non è multiplo di 8; classe, privato, binari.

**Righe 98–101**

```java
 98          } catch (RuntimeException ex) {
 99              error.setText(ex.getMessage() == null ? "Input non valido" : ex.getMessage());
100          }
101      }
```

Qualsiasi errore di input (lanciato da `IpUtils` o dalla conversione dei numeri) viene mostrato in rosso invece di bloccare il programma.

**Righe 103–104**

```java
103      private void add(String k, String v) { model.addRow(new Object[] {k, v}); }
104  }
```

Aggiunge una riga "campo, valore" alla tabella.
