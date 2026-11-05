# Riga per riga — pacchetto `ui` (3/3): svolgimento dell'esame e report

Questa parte copre lo schermo dell'esame (`ExamPanel`), il disegno della domanda (`QuestionView`), il riepilogo (`ItemReviewDialog`), il report (`ResultsPanel`) e la revisione (`ReviewDialog`). Descrizione generale: [06 - ui](../06-classi-ui.md); flowchart: [algoritmo 9](../08-algoritmi.md#9-svolgimento-dellesame-navigazione-timer-e-consegna).

**File in questo documento:** [`ExamPanel.java`](#exampaneljava) · [`QuestionView.java`](#questionviewjava) · [`ItemReviewDialog.java`](#itemreviewdialogjava) · [`ResultsPanel.java`](#resultspaneljava) · [`ReviewDialog.java`](#reviewdialogjava)

---

## ExamPanel.java

`src/main/java/com/lordoc/ui/ExamPanel.java` — lo schermo dell'esame in corso.

**Righe 1–26**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.model.ExamMode;
 4  import com.lordoc.model.ExamSession;
 5  import com.lordoc.model.Question;
 6  import com.lordoc.util.Text;
 7  
 8  import javax.swing.AbstractAction;
 9  import javax.swing.BorderFactory;
10  import javax.swing.JButton;
11  import javax.swing.JCheckBox;
12  import javax.swing.JComponent;
13  import javax.swing.JLabel;
14  import javax.swing.JOptionPane;
15  import javax.swing.JPanel;
16  import javax.swing.JProgressBar;
17  import javax.swing.KeyStroke;
18  import javax.swing.Timer;
19  import java.awt.BorderLayout;
20  import java.awt.Color;
21  import java.awt.FlowLayout;
22  import java.awt.GridLayout;
23  import java.awt.event.ActionEvent;
24  import java.awt.event.InputEvent;
25  import java.awt.event.KeyEvent;
26  import java.util.function.Consumer;
```

Pacchetto e import (modello, componenti Swing, tastiera, timer).

**Righe 28–30**

```java
28  /** Schermata di svolgimento dell'esame. */
29  public final class ExamPanel extends JPanel {
30      private static final long serialVersionUID = 1L;
```

Inizio della classe.

**Righe 32–39**

```java
32      private final ExamSession session;
33      private final Consumer<ExamSession> onFinish;
34      private final QuestionView view = new QuestionView();
35      private final JLabel counterLabel = Ui.label("", Ui.BOLD.deriveFont(17f), Ui.HEADER_FG);
36      private final JLabel timerLabel = Ui.label("", Ui.MONO.deriveFont(java.awt.Font.BOLD, 20f), Ui.HEADER_FG);
37      private final JLabel infoLabel = Ui.label("", Ui.SMALL, new Color(0x33475B));
38      private final JCheckBox markBox = new JCheckBox("Marca per revisione (Ctrl+M)");
39      private final JProgressBar progress = new JProgressBar();
```

Campi: la sessione; `onFinish`, l'azione da eseguire alla consegna (fornita da `MainFrame`); la vista della domanda; le etichette di contatore, cronometro e informazioni; la casella "Marca"; la barra di avanzamento.

**Righe 40–45**

```java
40      private final JButton prevBtn;
41      private final JButton nextBtn;
42      private final JButton reviewBtn;
43      private final JButton showAnswerBtn;
44      private final Timer timer;
45      private boolean finished;
```

I pulsanti, il timer e l'interruttore `finished` che impedisce la doppia consegna.

**Righe 47–50**

```java
47      public ExamPanel(ExamSession session, Consumer<ExamSession> onFinish) {
48          super(new BorderLayout());
49          this.session = session;
50          this.onFinish = onFinish;
```

Costruttore: layout a zone (`BorderLayout`) e memorizzazione dei parametri.

**Righe 52–63**

```java
52          // --- barra superiore
53          JPanel top = new JPanel(new BorderLayout());
54          top.setBackground(Ui.HEADER_BG);
55          top.setBorder(Ui.padding(10, 18));
56          JLabel title = Ui.label(session.getTitle() + "  —  " + session.getMode().getLabel(), Ui.BOLD.deriveFont(17f),
57              Ui.HEADER_FG);
58          top.add(title, BorderLayout.WEST);
59          JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
60          center.setOpaque(false);
61          center.add(counterLabel);
62          top.add(center, BorderLayout.CENTER);
63          top.add(timerLabel, BorderLayout.EAST);
```

Barra blu: a sinistra "titolo — modalità", al centro il contatore "Domanda X di N" (in un pannello trasparente per centrarlo), a destra il cronometro.

**Righe 65–77**

```java
65          JPanel sub = new JPanel(new BorderLayout());
66          sub.setBackground(Ui.SUBHEADER_BG);
67          sub.setBorder(Ui.padding(6, 18));
68          markBox.setOpaque(false);
69          markBox.setFont(Ui.BOLD.deriveFont(13f));
70          markBox.addActionListener(e -> session.setMarked(session.getCurrent(), markBox.isSelected()));
71          sub.add(markBox, BorderLayout.WEST);
72          sub.add(infoLabel, BorderLayout.EAST);
73          progress.setMaximum(session.size());
74          progress.setStringPainted(true);
75          progress.setBorder(BorderFactory.createEmptyBorder(2, 30, 2, 30));
76          progress.setOpaque(false);
77          sub.add(progress, BorderLayout.CENTER);
```

Sottobarra azzurra: a sinistra la casella "Marca" (cliccandola aggiorna la marcatura della domanda corrente), a destra tipo/dominio, al centro la barra di avanzamento (massimo = numero di domande, testo visibile, margini laterali).

**Righe 79–82**

```java
79          JPanel north = new JPanel(new GridLayout(2, 1));
80          north.add(top);
81          north.add(sub);
82          add(north, BorderLayout.NORTH);
```

Le due barre impilate (griglia 2 righe × 1 colonna) nella zona alta.

**Righe 84–86**

```java
84          // --- domanda
85          view.setOnChange(this::refreshStatus);
86          add(view, BorderLayout.CENTER);
```

La vista della domanda al centro; ogni volta che l'utente modifica una risposta chiama `refreshStatus` per aggiornare la barra.

**Righe 88–94**

```java
88          // --- barra inferiore
89          prevBtn = Ui.button("◀ Precedente", () -> go(session.getCurrent() - 1));
90          nextBtn = Ui.button("Successiva ▶", this::next);
91          reviewBtn = Ui.button("Riepilogo domande", this::openItemReview);
92          showAnswerBtn = Ui.button("Mostra risposta", this::revealCurrent);
93          JButton endBtn = Ui.button("Termina esame", this::confirmEnd);
94          endBtn.setForeground(Ui.KO);
```

Creazione dei pulsanti, ciascuno con la sua azione: precedente, successiva, riepilogo, mostra risposta, termina (in rosso).

**Righe 96–110**

```java
 96          JPanel bottom = new JPanel(new BorderLayout());
 97          bottom.setBorder(BorderFactory.createCompoundBorder(
 98              BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xC5CED9)), Ui.padding(8, 14)));
 99          JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
100          left.add(endBtn);
101          left.add(reviewBtn);
102          if (session.getMode() == ExamMode.STUDY) {
103              left.add(showAnswerBtn);
104          }
105          JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
106          right.add(prevBtn);
107          right.add(nextBtn);
108          bottom.add(left, BorderLayout.WEST);
109          bottom.add(right, BorderLayout.EAST);
110          add(bottom, BorderLayout.SOUTH);
```

Barra inferiore con una linea di separazione in alto: a sinistra Termina e Riepilogo (più "Mostra risposta" solo in Studio), a destra Precedente e Successiva.

**Righe 112–115**

```java
112          if (!session.isAllowBack()) {
113              prevBtn.setVisible(false);
114              reviewBtn.setVisible(false);
115          }
```

Modalità "come l'esame reale": nasconde Precedente e Riepilogo.

**Riga 117**

```java
117          installKeys();
```

Registra le scorciatoie da tastiera.

**Righe 119–123**

```java
119          timer = new Timer(1000, e -> tick());
120          timer.start();
121          updateTimer();
122          go(0);
123      }
```

Crea la "sveglia" che ogni 1000 ms chiama `tick`, la avvia, mostra subito il tempo iniziale e visualizza la prima domanda.

**Righe 125–139**

```java
125      private void installKeys() {
126          bind(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), "next", this::next);
127          bind(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), "prev", () -> {
128              if (prevBtn.isVisible() && prevBtn.isEnabled()) {
129                  go(session.getCurrent() - 1);
130              }
131          });
132          bind(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), "mark", () -> {
133              markBox.setSelected(!markBox.isSelected());
134              session.setMarked(session.getCurrent(), markBox.isSelected());
135          });
136          if (session.getMode() == ExamMode.STUDY) {
137              bind(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "reveal", this::revealCurrent);
138          }
139      }
```

Scorciatoie: Alt+→ successiva; Alt+← precedente (solo se il pulsante è visibile e attivo); Ctrl+M inverte la marcatura; Ctrl+R mostra la risposta (solo in Studio).

**Righe 141–148**

```java
141      private void bind(KeyStroke ks, String name, Runnable r) {
142          getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, name);
143          getActionMap().put(name, new AbstractAction() {
144              private static final long serialVersionUID = 1L;
145              @Override
146              public void actionPerformed(ActionEvent e) { r.run(); }
147          });
148      }
```

Collega un tasto a un'azione in due passi: la `InputMap` associa il tasto a un nome (valida finché la finestra ha il focus, `WHEN_IN_FOCUSED_WINDOW`), la `ActionMap` associa il nome a un'azione (una classe anonima `AbstractAction` che esegue `r`).

**Riga 150**

```java
150      // ------------------------------------------------------------------ navigazione
```

Separatore: navigazione tra le domande.

**Righe 152–166**

```java
152      private void go(int index) {
153          if (index < 0 || index >= session.size()) {
154              return;
155          }
156          session.setCurrent(index);
157          Question q = session.getQuestion(index);
158          boolean revealed = session.isRevealed(index);
159          view.display(q, session.getResponse(index), !revealed, revealed);
160          markBox.setSelected(session.isMarked(index));
161          counterLabel.setText("Domanda " + (index + 1) + " di " + session.size());
162          prevBtn.setEnabled(index > 0);
163          nextBtn.setText(index == session.size() - 1 ? "Fine ▶" : "Successiva ▶");
164          showAnswerBtn.setEnabled(!revealed);
165          refreshStatus();
166      }
```

Mostra la domanda `index` (ignora indici fuori range): aggiorna la posizione corrente, ridisegna la domanda (modificabile solo se non rivelata, con correzione se rivelata), sincronizza la casella "Marca", il contatore, l'abilitazione di Precedente (non sulla prima), il testo di Successiva ("Fine ▶" sull'ultima) e di Mostra risposta.

**Righe 168–184**

```java
168      private void next() {
169          int cur = session.getCurrent();
170          if (!session.isAllowBack() && !session.isAnswered(cur)) {
171              int c = JOptionPane.showConfirmDialog(this,
172                  "Non hai risposto completamente a questa domanda.\n"
173                      + "In questa modalità non potrai tornare indietro. Proseguire?",
174                  "Domanda senza risposta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
175              if (c != JOptionPane.YES_OPTION) {
176                  return;
177              }
178          }
179          if (cur == session.size() - 1) {
180              confirmEnd();
181          } else {
182              go(cur + 1);
183          }
184      }
```

Successiva (vedi flowchart): se non si può tornare indietro e la risposta è incompleta chiede conferma; sull'ultima domanda propone la consegna, altrimenti avanza.

**Righe 186–193**

```java
186      private void revealCurrent() {
187          int cur = session.getCurrent();
188          if (session.getMode() != ExamMode.STUDY || session.isRevealed(cur)) {
189              return;
190          }
191          session.setRevealed(cur, true);
192          go(cur);
193      }
```

(Solo Studio, solo se non già rivelata) segna la domanda come rivelata e la ridisegna con correzione e spiegazione.

**Righe 195–205**

```java
195      private void refreshStatus() {
196          int answered = session.countAnswered();
197          progress.setValue(answered);
198          progress.setString(answered + " / " + session.size() + " risposte");
199          Question q = session.getQuestion(session.getCurrent());
200          String info = q.getType().getLabel();
201          if (session.getMode() == ExamMode.STUDY) {
202              info = q.getDomain().getLabel() + "  ·  " + info;
203          }
204          infoLabel.setText(info);
205      }
```

Aggiorna la barra ("12 / 75 risposte") e l'etichetta informativa: tipo di domanda, preceduto dal dominio in modalità Studio (in Simulazione il dominio non si mostra, come all'esame).

**Righe 207–215**

```java
207      private void openItemReview() {
208          ItemReviewDialog d = new ItemReviewDialog(javax.swing.SwingUtilities.getWindowAncestor(this), session);
209          d.setVisible(true);
210          if (d.isEndRequested()) {
211              confirmEnd();
212          } else if (d.getSelectedIndex() >= 0) {
213              go(d.getSelectedIndex());
214          }
215      }
```

Apre il riepilogo (modale); alla chiusura, se l'utente ha chiesto di terminare chiede conferma di consegna, altrimenti se ha scelto una domanda ci salta.

**Riga 217**

```java
217      // ------------------------------------------------------------------ tempo e fine
```

Separatore: tempo e consegna.

**Righe 219–228**

```java
219      private void tick() {
220          session.tick();
221          updateTimer();
222          if (session.isTimed() && session.getRemainingSeconds() == 0) {
223              timer.stop();
224              JOptionPane.showMessageDialog(this, "Tempo scaduto! L'esame viene consegnato.",
225                  "Tempo scaduto", JOptionPane.WARNING_MESSAGE);
226              finish();
227          }
228      }
```

Ogni secondo: +1 secondo, aggiorna il cronometro; se l'esame è a tempo e il tempo è finito ferma il timer, avvisa e consegna.

**Righe 230–238**

```java
230      private void updateTimer() {
231          if (session.isTimed()) {
232              int rem = session.getRemainingSeconds();
233              timerLabel.setText("⏱ " + Text.formatDuration(rem));
234              timerLabel.setForeground(rem <= 300 ? new Color(0xFFB4A9) : Ui.HEADER_FG);
235          } else {
236              timerLabel.setText("⏱ " + Text.formatDuration(session.getElapsedSeconds()));
237          }
238      }
```

Esame a tempo: mostra il tempo rimanente, in rosa negli ultimi 5 minuti (300 secondi). Altrimenti mostra il tempo trascorso.

**Righe 240–254**

```java
240      private void confirmEnd() {
241          int unanswered = session.size() - session.countAnswered();
242          String msg = "Vuoi terminare e consegnare l'esame?";
243          if (unanswered > 0) {
244              msg = "Ci sono ancora " + unanswered + " domande senza risposta completa.\n" + msg;
245          }
246          if (session.countMarked() > 0) {
247              msg = "Hai " + session.countMarked() + " domande marcate per revisione.\n" + msg;
248          }
249          int c = JOptionPane.showConfirmDialog(this, msg, "Termina esame", JOptionPane.YES_NO_OPTION,
250              JOptionPane.QUESTION_MESSAGE);
251          if (c == JOptionPane.YES_OPTION) {
252              finish();
253          }
254      }
```

Prepara il messaggio di consegna aggiungendo in testa gli avvisi su domande incomplete e marcate, poi chiede conferma; se confermato consegna.

**Righe 256–263**

```java
256      private void finish() {
257          if (finished) {
258              return;
259          }
260          finished = true;
261          timer.stop();
262          onFinish.accept(session);
263      }
```

Consegna una sola volta: se `finished` è già vero esce; altrimenti lo imposta, ferma il timer e passa la sessione a `onFinish`.

**Righe 265–270**

```java
265      /** Ferma il timer senza consegnare (es. chiusura finestra). */
266      public void abort() {
267          finished = true;
268          timer.stop();
269      }
270  }
```

`abort`: segna come finito e ferma il timer senza consegnare (usato quando si abbandona l'esame).

---

## QuestionView.java

`src/main/java/com/lordoc/ui/QuestionView.java` — il disegno di una domanda e dell'area di risposta.

**Righe 1–32**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.model.Question;
 4  import com.lordoc.model.QuestionType;
 5  import com.lordoc.model.Response;
 6  
 7  import javax.swing.AbstractButton;
 8  import javax.swing.BorderFactory;
 9  import javax.swing.ButtonGroup;
10  import javax.swing.JCheckBox;
11  import javax.swing.JComboBox;
12  import javax.swing.JLabel;
13  import javax.swing.JPanel;
14  import javax.swing.JRadioButton;
15  import javax.swing.JScrollPane;
16  import javax.swing.JTextArea;
17  import javax.swing.JTextField;
18  import javax.swing.ScrollPaneConstants;
19  import javax.swing.SwingUtilities;
20  import javax.swing.event.DocumentEvent;
21  import javax.swing.event.DocumentListener;
22  import java.awt.BorderLayout;
23  import java.awt.Color;
24  import java.awt.Dimension;
25  import java.awt.FlowLayout;
26  import java.awt.GridBagConstraints;
27  import java.awt.GridBagLayout;
28  import java.awt.Insets;
29  import java.awt.event.MouseAdapter;
30  import java.awt.event.MouseEvent;
31  import java.util.ArrayList;
32  import java.util.List;
```

Pacchetto e import.

**Righe 34–37**

```java
34  /**
35   * Visualizza una domanda con l'area di risposta adatta al tipo (radio, checkbox,
36   * abbinamento, campo di testo). In modalità "reveal" mostra correzione e spiegazione.
37   */
```

Commento della classe.

**Righe 38–39**

```java
38  public final class QuestionView extends JPanel {
39      private static final long serialVersionUID = 1L;
```

Inizio della classe.

**Righe 41–43**

```java
41      private final ScrollablePanel content = new ScrollablePanel(new GridBagLayout());
42      private final JScrollPane scroll;
43      private Runnable onChange = () -> { };
```

Il contenuto (che segue la larghezza della finestra), il pannello di scorrimento che lo contiene e l'azione da eseguire quando la risposta cambia (all'inizio "non fare nulla").

**Righe 45–55**

```java
45      public QuestionView() {
46          super(new BorderLayout());
47          content.setBackground(Ui.PANEL_BG);
48          content.setBorder(Ui.padding(16, 22));
49          scroll = new JScrollPane(content);
50          scroll.setBorder(null);
51          scroll.getViewport().setBackground(Ui.PANEL_BG);
52          scroll.getVerticalScrollBar().setUnitIncrement(20);
53          scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
54          add(scroll, BorderLayout.CENTER);
55      }
```

Costruttore: sfondo bianco e margini del contenuto, scorrimento solo verticale (20 pixel per scatto), anche lo sfondo della zona di scorrimento bianco.

**Riga 57**

```java
57      public void setOnChange(Runnable r) { this.onChange = r == null ? () -> { } : r; }
```

Imposta l'azione "risposta cambiata" (un `null` diventa "non fare nulla", per non doverlo controllare ovunque).

**Righe 59–62**

```java
59      /**
60       * @param editable se l'utente può modificare la risposta
61       * @param reveal   se mostrare la correzione e la spiegazione
62       */
```

Commento di `display`.

**Righe 63–65**

```java
63      public void display(Question q, Response r, boolean editable, boolean reveal) {
64          content.removeAll();
65          int y = 0;
```

Ricostruisce tutto da zero: svuota il contenuto; `y` tiene il numero di riga del GridBag.

**Righe 67–68**

```java
67          WrapLabel text = new WrapLabel(q.getDisplayText(), Ui.BASE.deriveFont(16f));
68          content.add(text, Ui.row(y++));
```

Il testo della domanda (16 pt, va a capo).

**Righe 70–72**

```java
70          if (q.hasExhibit()) {
71              content.add(exhibit(q.getExhibit()), Ui.row(y++));
72          }
```

L'exhibit, se c'è.

**Righe 74–75**

```java
74          JPanel answers = new JPanel(new GridBagLayout());
75          answers.setOpaque(false);
```

Pannello trasparente per l'area di risposta.

**Righe 76–89**

```java
76          switch (q.getType()) {
77              case SINGLE:
78              case MULTI:
79                  buildChoices(answers, q, r, editable, reveal);
80                  break;
81              case DRAG:
82                  buildDrag(answers, q, r, editable, reveal);
83                  break;
84              case FILL:
85                  buildFill(answers, q, r, editable, reveal);
86                  break;
87              default:
88                  break;
89          }
```

Sceglie come costruire l'area di risposta in base al tipo.

**Righe 90–92**

```java
90          GridBagConstraints ac = Ui.row(y++);
91          ac.insets = new Insets(12, 0, 4, 0);
92          content.add(answers, ac);
```

Aggiunge l'area di risposta con un po' di spazio in più sopra.

**Righe 94–96**

```java
94          if (reveal) {
95              content.add(feedback(q, r), Ui.row(y++));
96          }
```

In revisione aggiunge il riquadro con la correzione.

**Righe 97–101**

```java
 97          Ui.addFiller(content, y);
 98          content.revalidate();
 99          content.repaint();
100          SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
101      }
```

Spinge tutto in alto, chiede di ricalcolare e ridisegnare il layout e, appena possibile (`invokeLater`), riporta lo scorrimento in cima.

**Riga 103**

```java
103      // ------------------------------------------------------------------ exhibit
```

Separatore: costruzione dell'exhibit.

**Righe 105–110**

```java
105      private JPanel exhibit(String exhibitText) {
106          JPanel p = new JPanel(new BorderLayout());
107          p.setOpaque(false);
108          JLabel title = Ui.label("Exhibit", Ui.BOLD.deriveFont(13f), Ui.ACCENT);
109          title.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
110          p.add(title, BorderLayout.NORTH);
```

Pannello con il titolo "Exhibit" in blu.

**Righe 111–117**

```java
111          JTextArea area = new JTextArea(exhibitText);
112          area.setFont(Ui.MONO);
113          area.setEditable(false);
114          area.setBackground(Ui.EXHIBIT_BG);
115          area.setForeground(Ui.EXHIBIT_FG);
116          area.setCaretColor(Ui.EXHIBIT_FG);
117          area.setBorder(Ui.padding(10, 12));
```

Area di testo monospazio, non modificabile, sfondo nero e testo chiaro come un terminale, con margine interno.

**Righe 118–120**

```java
118          JScrollPane sp = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
119              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
120          sp.setBorder(BorderFactory.createLineBorder(new Color(0x444444)));
```

Barra di scorrimento solo orizzontale (righe lunghe), con bordo grigio scuro.

**Righe 121–126**

```java
121          int h = area.getPreferredSize().height + sp.getHorizontalScrollBar().getPreferredSize().height + 4;
122          sp.setPreferredSize(new Dimension(100, h));
123          sp.setMinimumSize(new Dimension(100, h));
124          p.add(sp, BorderLayout.CENTER);
125          return p;
126      }
```

Fissa l'altezza in modo da mostrare tutte le righe più lo spazio della barra orizzontale (altrimenti il layout potrebbe schiacciarlo).

**Riga 128**

```java
128      // ------------------------------------------------------------------ choices
```

Separatore: domande a scelta.

**Righe 130–133**

```java
130      private void buildChoices(JPanel panel, Question q, Response r, boolean editable, boolean reveal) {
131          boolean single = q.getType() == QuestionType.SINGLE;
132          ButtonGroup group = new ButtonGroup();
133          List<String> opts = q.getOptions();
```

`single` dice se è SINGLE; il `ButtonGroup` garantisce che un solo pallino sia selezionato.

**Righe 134–135**

```java
134          for (int i = 0; i < opts.size(); i++) {
135              final int idx = i;
```

Per ogni opzione. `final int idx = i` serve perché le lambda possono usare solo variabili che non cambiano.

**Righe 136–145**

```java
136              AbstractButton btn = single ? new JRadioButton() : new JCheckBox();
137              btn.setText(Question.letter(i) + ".");
138              btn.setFont(Ui.BOLD);
139              btn.setOpaque(false);
140              btn.setSelected(r.getSelected().contains(i));
141              btn.setEnabled(editable);
142              btn.setVerticalAlignment(AbstractButton.TOP);
143              if (single) {
144                  group.add(btn);
145              }
```

Pallino o casella, con la lettera ("A.") in grassetto; selezionato se la risposta già lo contiene; disattivato se non modificabile; allineato in alto (per opzioni su più righe). I pallini vengono aggiunti al gruppo.

**Righe 146–153**

```java
146              btn.addItemListener(e -> {
147                  if (btn.isSelected()) {
148                      r.select(idx, single);
149                  } else {
150                      r.deselect(idx);
151                  }
152                  onChange.run();
153              });
```

Quando cambia lo stato: se selezionato registra la scelta (svuotando le altre se SINGLE), altrimenti la rimuove; poi avvisa che la risposta è cambiata.

**Righe 155–168**

```java
155              WrapLabel lbl = new WrapLabel(opts.get(i), Ui.BASE);
156              lbl.setBorder(BorderFactory.createEmptyBorder(3, 4, 0, 0));
157              if (editable) {
158                  lbl.addMouseListener(new MouseAdapter() {
159                      @Override
160                      public void mouseClicked(MouseEvent e) {
161                          if (single) {
162                              btn.setSelected(true);
163                          } else {
164                              btn.setSelected(!btn.isSelected());
165                          }
166                      }
167                  });
168              }
```

Testo dell'opzione che va a capo. Se modificabile, un clic sul testo seleziona il pallino o inverte la casella (comodità: non serve centrare il bottoncino).

**Righe 170–174**

```java
170              JPanel row = new JPanel(new BorderLayout());
171              row.setBorder(Ui.padding(4, 6));
172              row.add(btn, BorderLayout.WEST);
173              row.add(lbl, BorderLayout.CENTER);
174              row.setOpaque(false);
```

La riga: bottone a sinistra, testo al centro.

**Righe 175–190**

```java
175              if (reveal) {
176                  boolean correct = q.getCorrectOptions().contains(i);
177                  boolean chosen = r.getSelected().contains(i);
178                  if (correct) {
179                      row.setOpaque(true);
180                      row.setBackground(Ui.OK_BG);
181                      row.add(Ui.label(chosen ? "✔ corretta (scelta)" : "✔ corretta", Ui.BOLD, Ui.OK),
182                          BorderLayout.EAST);
183                  } else if (chosen) {
184                      row.setOpaque(true);
185                      row.setBackground(Ui.KO_BG);
186                      row.add(Ui.label("✘ scelta errata", Ui.BOLD, Ui.KO), BorderLayout.EAST);
187                  }
188              }
189              panel.add(row, Ui.row(i));
190          }
```

In revisione: opzione corretta su sfondo verde con "✔ corretta" (o "corretta (scelta)"); opzione scelta ma sbagliata su sfondo rosa con "✘ scelta errata".

**Righe 191–195**

```java
191          if (!single && editable) {
192              JLabel hint = Ui.label("Seleziona " + q.requiredSelections() + " risposte.", Ui.SMALL, Color.GRAY);
193              panel.add(hint, Ui.row(opts.size()));
194          }
195      }
```

Per le MULTI aggiunge il promemoria "Seleziona N risposte".

**Riga 197**

```java
197      // ------------------------------------------------------------------ drag & drop (abbinamento)
```

Separatore: domande di abbinamento.

**Righe 199–201**

```java
199      private void buildDrag(JPanel panel, Question q, Response r, boolean editable, boolean reveal) {
200          JLabel hint = Ui.label("Abbina ogni elemento a sinistra alla categoria corretta.", Ui.SMALL, Color.GRAY);
201          panel.add(hint, Ui.row(0));
```

Istruzioni in grigio.

**Righe 202–205**

```java
202          List<String> targets = new ArrayList<>();
203          targets.add("");
204          targets.addAll(q.getDragTargets());
205          List<String> items = q.getDragItems();
```

Elenco delle voci dei menu: una voce vuota iniziale ("non ancora scelto") seguita dalle categorie.

**Righe 206–218**

```java
206          for (int i = 0; i < items.size(); i++) {
207              final int idx = i;
208              JPanel row = new JPanel(new GridBagLayout());
209              row.setBorder(Ui.padding(4, 6));
210              row.setOpaque(false);
211  
212              GridBagConstraints c = new GridBagConstraints();
213              c.gridx = 0;
214              c.weightx = 1;
215              c.fill = GridBagConstraints.HORIZONTAL;
216              c.insets = new Insets(0, 0, 0, 10);
217              WrapLabel item = new WrapLabel(items.get(i), Ui.BOLD);
218              row.add(item, c);
```

Per ogni elemento crea una riga GridBag: l'elemento in grassetto si allarga a sinistra.

**Righe 220–229**

```java
220              JComboBox<String> combo = new JComboBox<>(targets.toArray(new String[0]));
221              combo.setFont(Ui.BASE);
222              String cur = r.getDragAnswer(i);
223              combo.setSelectedItem(cur == null ? "" : cur);
224              combo.setEnabled(editable);
225              combo.setPrototypeDisplayValue(longest(targets));
226              combo.addActionListener(e -> {
227                  r.setDragAnswer(idx, (String) combo.getSelectedItem());
228                  onChange.run();
229              });
```

Menu a tendina con le categorie, preimpostato sulla scelta già fatta; largo quanto la voce più lunga (`setPrototypeDisplayValue`). Alla scelta salva l'abbinamento e avvisa.

**Righe 230–233**

```java
230              c.gridx = 1;
231              c.weightx = 0;
232              c.fill = GridBagConstraints.NONE;
233              row.add(combo, c);
```

Il menu nella seconda colonna, senza allargarsi.

**Righe 235–245**

```java
235              if (reveal) {
236                  String expected = q.getDragSolution().get(i);
237                  boolean ok = expected.equals(cur);
238                  row.setOpaque(true);
239                  row.setBackground(ok ? Ui.OK_BG : Ui.KO_BG);
240                  c.gridx = 2;
241                  row.add(Ui.label(ok ? "✔" : "✘ → " + expected, Ui.BOLD, ok ? Ui.OK : Ui.KO), c);
242              }
243              panel.add(row, Ui.row(i + 1));
244          }
245      }
```

In revisione la riga diventa verde con ✔ se giusta, rosa con "✘ → categoria giusta" se sbagliata.

**Righe 247–255**

```java
247      private static String longest(List<String> l) {
248          String best = "XXXXXXXXXX";
249          for (String s : l) {
250              if (s.length() > best.length()) {
251                  best = s;
252              }
253          }
254          return best;
255      }
```

Restituisce il testo più lungo dell'elenco (minimo 10 caratteri) per dimensionare i menu.

**Riga 257**

```java
257      // ------------------------------------------------------------------ fill
```

Separatore: risposta aperta.

**Righe 259–265**

```java
259      private void buildFill(JPanel panel, Question q, Response r, boolean editable, boolean reveal) {
260          JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
261          row.setOpaque(false);
262          row.add(Ui.label("Risposta:", Ui.BOLD, null));
263          JTextField field = new JTextField(r.getFillText(), 24);
264          field.setFont(Ui.MONO.deriveFont(16f));
265          field.setEditable(editable);
```

Riga con l'etichetta "Risposta:" e un campo di testo monospazio precompilato con la risposta già data.

**Righe 266–275**

```java
266          field.getDocument().addDocumentListener(new DocumentListener() {
267              private void update() {
268                  r.setFillText(field.getText());
269                  onChange.run();
270              }
271              @Override public void insertUpdate(DocumentEvent e) { update(); }
272              @Override public void removeUpdate(DocumentEvent e) { update(); }
273              @Override public void changedUpdate(DocumentEvent e) { update(); }
274          });
275          row.add(field);
```

Ascolta ogni modifica del testo (inserimento, cancellazione, cambio di attributi) e la registra subito nella risposta.

**Righe 276–280**

```java
276          if (reveal) {
277              boolean ok = q.isCorrect(r);
278              field.setBackground(ok ? Ui.OK_BG : Ui.KO_BG);
279          }
280          panel.add(row, Ui.row(0));
```

In revisione colora lo sfondo del campo di verde o rosa.

**Righe 281–284**

```java
281          if (editable) {
282              SwingUtilities.invokeLater(field::requestFocusInWindow);
283          }
284      }
```

Se modificabile, porta il cursore nel campo così si può scrivere subito.

**Riga 286**

```java
286      // ------------------------------------------------------------------ feedback
```

Separatore: il riquadro di correzione.

**Righe 288–294**

```java
288      private JPanel feedback(Question q, Response r) {
289          boolean ok = q.isCorrect(r);
290          JPanel p = new JPanel(new GridBagLayout());
291          p.setBackground(new Color(0xF6F8FB));
292          p.setBorder(BorderFactory.createCompoundBorder(
293              BorderFactory.createMatteBorder(0, 5, 0, 0, ok ? Ui.OK : Ui.KO), Ui.padding(10, 14)));
294          int y = 0;
```

Riquadro grigio chiaro con una barra colorata a sinistra (verde o rossa).

**Righe 295–302**

```java
295          String verdict;
296          if (ok) {
297              verdict = "✔ Risposta corretta";
298          } else if (!q.isAnswered(r)) {
299              verdict = "✘ Risposta mancante o incompleta";
300          } else {
301              verdict = "✘ Risposta errata";
302          }
```

Il verdetto: corretta, mancante/incompleta, oppure errata.

**Righe 303–318**

```java
303          p.add(Ui.label(verdict, Ui.BOLD.deriveFont(17f), ok ? Ui.OK : Ui.KO), Ui.row(y++));
304          p.add(new WrapLabel("Risposta corretta: " + q.getCorrectAnswerText(), Ui.BOLD), Ui.row(y++));
305          if (!q.getExplanation().isBlank()) {
306              JLabel h = Ui.label("Spiegazione", Ui.BOLD, Ui.ACCENT);
307              h.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
308              p.add(h, Ui.row(y++));
309              p.add(new WrapLabel(plain(q.getExplanation()), Ui.BASE), Ui.row(y++));
310          }
311          if (!q.getReference().isBlank()) {
312              p.add(new WrapLabel("Riferimento: " + q.getReference(), Ui.SMALL.deriveFont(java.awt.Font.ITALIC)),
313                  Ui.row(y++));
314          }
315          p.add(Ui.label("ID domanda: " + q.getId() + "  ·  " + q.getDomain().getLabel() + "  ·  "
316              + q.getType().getLabel(), Ui.SMALL, Color.GRAY), Ui.row(y));
317          return p;
318      }
```

Mostra verdetto e risposta corretta; se c'è una spiegazione aggiunge il titolo "Spiegazione" e il testo ripulito; se c'è un riferimento lo mostra in corsivo; in fondo ID, dominio e tipo in grigio.

**Righe 320–324**

```java
320      /** Rimuove la mini-sintassi `codice` e **grassetto** per la visualizzazione in testo semplice. */
321      static String plain(String s) {
322          return s.replace("`", "").replace("**", "");
323      }
324  }
```

`plain`: toglie gli apici inversi e gli asterischi doppi dalla spiegazione.

---

## ItemReviewDialog.java

`src/main/java/com/lordoc/ui/ItemReviewDialog.java` — il riepilogo delle domande durante l'esame.

**Righe 1–20**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.model.ExamMode;
 4  import com.lordoc.model.ExamSession;
 5  
 6  import javax.swing.JButton;
 7  import javax.swing.JDialog;
 8  import javax.swing.JPanel;
 9  import javax.swing.JScrollPane;
10  import javax.swing.JTable;
11  import javax.swing.ListSelectionModel;
12  import javax.swing.table.AbstractTableModel;
13  import javax.swing.table.DefaultTableCellRenderer;
14  import java.awt.BorderLayout;
15  import java.awt.Color;
16  import java.awt.Component;
17  import java.awt.FlowLayout;
18  import java.awt.Window;
19  import java.awt.event.MouseAdapter;
20  import java.awt.event.MouseEvent;
```

Pacchetto e import.

**Righe 22–24**

```java
22  /** Riepilogo di tutte le domande (risposte/incomplete/marcate) con salto rapido. */
23  public final class ItemReviewDialog extends JDialog {
24      private static final long serialVersionUID = 1L;
```

Inizio della classe (finestra secondaria).

**Righe 26–29**

```java
26      private final ExamSession session;
27      private final JTable table;
28      private int selectedIndex = -1;
29      private boolean endRequested;
```

Campi: sessione, tabella, indice scelto (−1 = nessuno) e richiesta di terminare.

**Righe 31–34**

```java
31      public ItemReviewDialog(Window owner, ExamSession session) {
32          super(owner, "Riepilogo domande", ModalityType.APPLICATION_MODAL);
33          this.session = session;
34          boolean study = session.getMode() == ExamMode.STUDY;
```

Costruttore: finestra modale; in Studio si mostra anche la colonna del dominio.

**Righe 36–55**

```java
36          table = new JTable(new AbstractTableModel() {
37              private static final long serialVersionUID = 1L;
38              private final String[] cols = study
39                  ? new String[] {"#", "Stato", "Marcata", "Dominio"}
40                  : new String[] {"#", "Stato", "Marcata"};
41  
42              @Override public int getRowCount() { return session.size(); }
43              @Override public int getColumnCount() { return cols.length; }
44              @Override public String getColumnName(int c) { return cols[c]; }
45  
46              @Override
47              public Object getValueAt(int r, int c) {
48                  switch (c) {
49                      case 0: return r + 1;
50                      case 1: return session.isAnswered(r) ? "Risposta data" : "Incompleta";
51                      case 2: return session.isMarked(r) ? "⚑ Marcata" : "";
52                      default: return session.getQuestion(r).getDomain().getLabel();
53                  }
54              }
55          });
```

Modello della tabella scritto come classe anonima: righe = domande; colonne #, Stato, Marcata (e Dominio). `getValueAt` restituisce il numero, "Risposta data"/"Incompleta", "⚑ Marcata" o vuoto, e il dominio.

**Righe 56–59**

```java
56          table.setRowHeight(24);
57          table.setFont(Ui.BASE.deriveFont(14f));
58          table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
59          table.getColumnModel().getColumn(0).setMaxWidth(60);
```

Aspetto e selezione singola; la prima colonna stretta.

**Righe 60–72**

```java
60          table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
61              private static final long serialVersionUID = 1L;
62              @Override
63              public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc,
64                                                             int row, int col) {
65                  Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
66                  if (!sel) {
67                      c.setForeground(col == 1 && !session.isAnswered(row) ? Ui.KO
68                          : col == 2 ? Ui.WARN : Color.BLACK);
69                  }
70                  return c;
71              }
72          });
```

Un *renderer* decide come disegnare ogni cella: qui (se la riga non è selezionata) colora di rosso "Incompleta" e di arancio la colonna "Marcata".

**Righe 73–80**

```java
73          table.addMouseListener(new MouseAdapter() {
74              @Override
75              public void mouseClicked(MouseEvent e) {
76                  if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
77                      jump(table.getSelectedRow());
78                  }
79              }
80          });
```

Doppio clic su una riga: salta a quella domanda.

**Righe 81–83**

```java
81          if (session.getCurrent() < session.size()) {
82              table.setRowSelectionInterval(session.getCurrent(), session.getCurrent());
83          }
```

Seleziona all'apertura la riga della domanda corrente.

**Righe 85–99**

```java
85          JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
86          buttons.add(Ui.button("Vai alla selezionata", () -> {
87              if (table.getSelectedRow() >= 0) {
88                  jump(table.getSelectedRow());
89              }
90          }));
91          buttons.add(Ui.button("Prima incompleta", () -> jumpFirst(false)));
92          buttons.add(Ui.button("Prima marcata", () -> jumpFirst(true)));
93          JButton end = Ui.button("Termina esame", () -> {
94              endRequested = true;
95              dispose();
96          });
97          end.setForeground(Ui.KO);
98          buttons.add(end);
99          buttons.add(Ui.button("Chiudi", this::dispose));
```

Pulsanti: vai alla selezionata, prima incompleta, prima marcata, termina esame (rosso, imposta `endRequested` e chiude), chiudi.

**Righe 101–109**

```java
101          JPanel root = new JPanel(new BorderLayout());
102          root.add(Ui.header("Riepilogo", session.countAnswered() + " risposte su " + session.size()
103              + "  ·  " + session.countMarked() + " marcate"), BorderLayout.NORTH);
104          root.add(new JScrollPane(table), BorderLayout.CENTER);
105          root.add(buttons, BorderLayout.SOUTH);
106          setContentPane(root);
107          setSize(720, 560);
108          setLocationRelativeTo(owner);
109      }
```

Intestazione con i conteggi, tabella, pulsanti; dimensione e posizione.

**Righe 111–114**

```java
111      private void jump(int idx) {
112          selectedIndex = idx;
113          dispose();
114      }
```

Memorizza la domanda scelta e chiude la finestra.

**Righe 116–125**

```java
116      private void jumpFirst(boolean marked) {
117          for (int i = 0; i < session.size(); i++) {
118              if (marked ? session.isMarked(i) : !session.isAnswered(i)) {
119                  jump(i);
120                  return;
121              }
122          }
123          javax.swing.JOptionPane.showMessageDialog(this,
124              marked ? "Nessuna domanda marcata." : "Hai risposto a tutte le domande.");
125      }
```

Cerca la prima domanda marcata (o la prima incompleta) e ci salta; se non ce ne sono lo dice.

**Righe 127–130**

```java
127      public int getSelectedIndex() { return selectedIndex; }
128  
129      public boolean isEndRequested() { return endRequested; }
130  }
```

Getter letti da `ExamPanel` dopo la chiusura.

---

## ResultsPanel.java

`src/main/java/com/lordoc/ui/ResultsPanel.java` — il report finale.

**Righe 1–32**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.model.Domain;
 4  import com.lordoc.model.DomainScore;
 5  import com.lordoc.model.ExamResult;
 6  import com.lordoc.model.ExamSession;
 7  import com.lordoc.model.Question;
 8  import com.lordoc.util.Text;
 9  
10  import javax.swing.BorderFactory;
11  import javax.swing.JComboBox;
12  import javax.swing.JLabel;
13  import javax.swing.JPanel;
14  import javax.swing.JProgressBar;
15  import javax.swing.JScrollPane;
16  import javax.swing.JSplitPane;
17  import javax.swing.JTable;
18  import javax.swing.ListSelectionModel;
19  import javax.swing.SwingUtilities;
20  import javax.swing.table.AbstractTableModel;
21  import javax.swing.table.DefaultTableCellRenderer;
22  import java.awt.BorderLayout;
23  import java.awt.Color;
24  import java.awt.Component;
25  import java.awt.FlowLayout;
26  import java.awt.GridBagLayout;
27  import java.awt.event.MouseAdapter;
28  import java.awt.event.MouseEvent;
29  import java.util.ArrayList;
30  import java.util.List;
31  import java.util.Map;
32  import java.util.function.Consumer;
```

Pacchetto e import.

**Righe 34–36**

```java
34  /** Report finale dell'esame: punteggio, dettaglio per dominio, revisione domande. */
35  public final class ResultsPanel extends JPanel {
36      private static final long serialVersionUID = 1L;
```

Inizio della classe.

**Riga 38**

```java
38      private static final String[] FILTERS = {"Tutte", "Solo errate / incomplete", "Solo corrette", "Solo marcate"};
```

Le voci del filtro.

**Righe 40–44**

```java
40      private final ExamResult result;
41      private final ExamSession session;
42      private final List<Integer> visible = new ArrayList<>();
43      private final JComboBox<String> filter = new JComboBox<>(FILTERS);
44      private final QuestionsModel questionsModel = new QuestionsModel();
```

Campi: risultato, sessione, la lista `visible` degli indici delle domande che passano il filtro, il menu del filtro e il modello della tabella.

**Righe 46–51**

```java
46      public ResultsPanel(ExamResult result, Runnable onHome, Consumer<List<Question>> onRetryWrong) {
47          super(new BorderLayout());
48          this.result = result;
49          this.session = result.getSession();
50  
51          add(buildHeader(), BorderLayout.NORTH);
```

Costruttore: riceve il risultato e le due azioni (tornare al menu, ripassare gli errori); in alto l'intestazione.

**Righe 53–59**

```java
53          JTable questions = new JTable(questionsModel);
54          questions.setRowHeight(24);
55          questions.setFont(Ui.BASE.deriveFont(14f));
56          questions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
57          questions.getColumnModel().getColumn(0).setMaxWidth(50);
58          questions.getColumnModel().getColumn(4).setMinWidth(120);
59          questions.getColumnModel().getColumn(4).setMaxWidth(140);
```

Tabella delle domande: altezza righe, carattere, selezione singola, larghezze di prima e ultima colonna.

**Righe 60–73**

```java
60          questions.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
61              private static final long serialVersionUID = 1L;
62              @Override
63              public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc,
64                                                             int row, int col) {
65                  Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
66                  if (!sel) {
67                      int idx = visible.get(row);
68                      boolean ok = session.getQuestion(idx).isCorrect(session.getResponse(idx));
69                      c.setForeground(col == 4 ? (ok ? Ui.OK : Ui.KO) : Color.BLACK);
70                  }
71                  return c;
72              }
73          });
```

Colora la colonna Esito di verde o rosso secondo la correttezza della domanda di quella riga.

**Righe 74–81**

```java
74          questions.addMouseListener(new MouseAdapter() {
75              @Override
76              public void mouseClicked(MouseEvent e) {
77                  if (e.getClickCount() == 2 && questions.getSelectedRow() >= 0) {
78                      openReview(questions.getSelectedRow());
79                  }
80              }
81          });
```

Doppio clic: apre la revisione dalla riga cliccata.

**Righe 82–83**

```java
82          filter.addActionListener(e -> applyFilter());
83          applyFilter();
```

Cambiando filtro si ricalcola l'elenco; un primo calcolo all'avvio.

**Righe 85–92**

```java
85          JPanel qPanel = new JPanel(new BorderLayout());
86          qPanel.setBorder(Ui.titled("Domande (doppio clic per la revisione con spiegazione)"));
87          JPanel qTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
88          qTop.add(new JLabel("Mostra:"));
89          qTop.add(filter);
90          qTop.add(Ui.button("Rivedi selezionata", () -> openReview(Math.max(0, questions.getSelectedRow()))));
91          qPanel.add(qTop, BorderLayout.NORTH);
92          qPanel.add(new JScrollPane(questions), BorderLayout.CENTER);
```

Riquadro domande con titolo, la riga filtro + "Rivedi selezionata" (se nessuna riga è selezionata parte dalla prima) e la tabella.

**Righe 94–97**

```java
94          JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildDomainPanel(), qPanel);
95          split.setResizeWeight(0.42);
96          split.setBorder(Ui.padding(8, 8));
97          add(split, BorderLayout.CENTER);
```

Domini a sinistra (42%) e domande a destra.

**Righe 99–112**

```java
 99          JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
100          bottom.add(Ui.button("Ripassa errori in modalità Studio", () -> {
101              List<Question> wrong = new ArrayList<>();
102              for (int i = 0; i < session.size(); i++) {
103                  if (!session.getQuestion(i).isCorrect(session.getResponse(i))) {
104                      wrong.add(session.getQuestion(i));
105                  }
106              }
107              if (wrong.isEmpty()) {
108                  javax.swing.JOptionPane.showMessageDialog(this, "Nessun errore: complimenti!");
109              } else {
110                  onRetryWrong.accept(wrong);
111              }
112          }));
```

Pulsante "Ripassa errori": raccoglie le domande non corrette; se non ce ne sono si congratula, altrimenti chiama l'azione ricevuta.

**Righe 113–115**

```java
113          bottom.add(Ui.button("Torna al menu principale", onHome));
114          add(bottom, BorderLayout.SOUTH);
115      }
```

Pulsante per tornare al menu.

**Righe 117–132**

```java
117      private JPanel buildHeader() {
118          JPanel h = new JPanel(new BorderLayout(20, 0));
119          h.setBackground(Color.WHITE);
120          h.setBorder(BorderFactory.createCompoundBorder(
121              BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC5CED9)), Ui.padding(14, 22)));
122  
123          JPanel left = new JPanel(new GridBagLayout());
124          left.setOpaque(false);
125          left.add(Ui.label(result.getTitle() + " — " + result.getMode().getLabel(), Ui.TITLE, Ui.HEADER_BG),
126              Ui.row(0));
127          boolean pass = result.isPassed();
128          left.add(Ui.label(pass ? "SUPERATO" : "NON SUPERATO", Ui.TITLE, pass ? Ui.OK : Ui.KO), Ui.row(1));
129          left.add(Ui.label(String.format("%d corrette su %d (%.1f%%)  ·  Tempo impiegato: %s",
130              result.getCorrect(), result.getTotal(), result.getPercent(),
131              Text.formatDuration(result.getDurationSeconds())), Ui.BASE, null), Ui.row(2));
132          h.add(left, BorderLayout.WEST);
```

Intestazione bianca con linea inferiore. A sinistra: titolo, SUPERATO/NON SUPERATO in verde/rosso, riga con corrette, percentuale (`%.1f` = un decimale) e tempo.

**Righe 134–146**

```java
134          JPanel right = new JPanel(new BorderLayout());
135          right.setOpaque(false);
136          JLabel score = Ui.label(result.getScaledScore() + " / " + ExamResult.MAX_SCORE, Ui.HUGE,
137              pass ? Ui.OK : Ui.KO);
138          score.setHorizontalAlignment(JLabel.CENTER);
139          right.add(score, BorderLayout.NORTH);
140          right.add(new ScoreBar(result.getScaledScore()), BorderLayout.CENTER);
141          JLabel note = Ui.label("Punteggio stimato su scala 300-1000. Cisco non pubblica la soglia ufficiale: "
142              + ExamResult.PASSING_SCORE + " è un obiettivo prudente.", Ui.SMALL, Color.GRAY);
143          right.add(note, BorderLayout.SOUTH);
144          h.add(right, BorderLayout.CENTER);
145          return h;
146      }
```

A destra: il punteggio grande, la barra con la soglia e la nota che spiega che la soglia è una stima.

**Righe 148–168**

```java
148      private JPanel buildDomainPanel() {
149          JPanel p = new JPanel(new GridBagLayout());
150          p.setBorder(Ui.titled("Risultato per dominio (exam topic)"));
151          int y = 0;
152          for (Map.Entry<Domain, DomainScore> e : result.getByDomain().entrySet()) {
153              DomainScore ds = e.getValue();
154              p.add(Ui.label(e.getKey().getLabel() + "  (" + e.getKey().getWeight() + "% dell'esame)", Ui.BOLD, null),
155                  Ui.row(y++));
156              JProgressBar bar = new JProgressBar(0, 100);
157              bar.setValue((int) Math.round(ds.getPercent()));
158              bar.setStringPainted(true);
159              bar.setString(ds.getCorrect() + " / " + ds.getTotal() + "  (" + Ui.pct(ds.getPercent()) + ")");
160              bar.setForeground(ds.getPercent() >= 82.5 ? Ui.OK : ds.getPercent() >= 60 ? Ui.WARN : Ui.KO);
161              p.add(bar, Ui.row(y++));
162          }
163          JLabel tip = Ui.label("<html><i>Verde ≥ 82,5% · Arancio ≥ 60% · Rosso &lt; 60%. "
164              + "Concentra lo studio sui domini in rosso con un esame personalizzato.</i></html>", Ui.SMALL, Color.GRAY);
165          p.add(tip, Ui.row(y++));
166          Ui.addFiller(p, y);
167          return p;
168      }
```

Per ogni dominio presente nel risultato: nome con peso e una barra con corrette/totale e percentuale, colorata verde (≥ 82,5%), arancio (≥ 60%) o rossa. In fondo la legenda (un'etichetta HTML per avere il corsivo e l'a capo).

**Righe 170–181**

```java
170      private void applyFilter() {
171          visible.clear();
172          int f = filter.getSelectedIndex();
173          for (int i = 0; i < session.size(); i++) {
174              boolean ok = session.getQuestion(i).isCorrect(session.getResponse(i));
175              boolean include = f == 0 || (f == 1 && !ok) || (f == 2 && ok) || (f == 3 && session.isMarked(i));
176              if (include) {
177                  visible.add(i);
178              }
179          }
180          questionsModel.fireTableDataChanged();
181      }
```

Ricostruisce `visible`: per ogni domanda verifica il filtro scelto (0 tutte, 1 errate, 2 corrette, 3 marcate) e notifica la tabella.

**Righe 183–189**

```java
183      private void openReview(int row) {
184          if (visible.isEmpty()) {
185              return;
186          }
187          new ReviewDialog(SwingUtilities.getWindowAncestor(this), session, new ArrayList<>(visible),
188              Math.min(row, visible.size() - 1)).setVisible(true);
189      }
```

Apre la revisione sulle domande filtrate, partendo dalla riga indicata (limitata all'ultima disponibile).

**Righe 191–216**

```java
191      private final class QuestionsModel extends AbstractTableModel {
192          private static final long serialVersionUID = 1L;
193          private final String[] cols = {"#", "ID", "Dominio", "Tipo", "Esito"};
194  
195          @Override public int getRowCount() { return visible.size(); }
196          @Override public int getColumnCount() { return cols.length; }
197          @Override public String getColumnName(int c) { return cols[c]; }
198  
199          @Override
200          public Object getValueAt(int r, int c) {
201              int idx = visible.get(r);
202              Question q = session.getQuestion(idx);
203              switch (c) {
204                  case 0: return idx + 1;
205                  case 1: return q.getId() + (session.isMarked(idx) ? " ⚑" : "");
206                  case 2: return q.getDomain().getLabel();
207                  case 3: return q.getType().getLabel();
208                  default:
209                      if (q.isCorrect(session.getResponse(idx))) {
210                          return "✔ Corretta";
211                      }
212                      return q.isAnswered(session.getResponse(idx)) ? "✘ Errata" : "✘ Mancante";
213              }
214          }
215      }
216  }
```

Modello della tabella: righe = domande visibili; valori: numero nell'esame, ID (con ⚑ se marcata), dominio, tipo, esito (corretta / errata / mancante).

---

## ReviewDialog.java

`src/main/java/com/lordoc/ui/ReviewDialog.java` — la revisione domanda per domanda.

**Righe 1–12**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.model.ExamSession;
 4  
 5  import javax.swing.JButton;
 6  import javax.swing.JDialog;
 7  import javax.swing.JLabel;
 8  import javax.swing.JPanel;
 9  import java.awt.BorderLayout;
10  import java.awt.FlowLayout;
11  import java.awt.Window;
12  import java.util.List;
```

Pacchetto e import.

**Righe 14–16**

```java
14  /** Revisione post-esame: mostra domanda, risposta data, risposta corretta e spiegazione. */
15  public final class ReviewDialog extends JDialog {
16      private static final long serialVersionUID = 1L;
```

Inizio della classe.

**Righe 18–24**

```java
18      private final ExamSession session;
19      private final List<Integer> indexes;
20      private final QuestionView view = new QuestionView();
21      private final JLabel counter = Ui.label("", Ui.BOLD, Ui.HEADER_FG);
22      private final JButton prev;
23      private final JButton next;
24      private int pos;
```

Campi: sessione, indici da rivedere, vista della domanda, etichetta del contatore, pulsanti e posizione corrente nella lista.

**Righe 26–30**

```java
26      public ReviewDialog(Window owner, ExamSession session, List<Integer> indexes, int startPos) {
27          super(owner, "Revisione domande", ModalityType.APPLICATION_MODAL);
28          this.session = session;
29          this.indexes = indexes;
30          this.pos = startPos;
```

Costruttore: finestra modale, memorizzazione dei parametri.

**Righe 32–35**

```java
32          JPanel top = new JPanel(new BorderLayout());
33          top.setBackground(Ui.HEADER_BG);
34          top.setBorder(Ui.padding(10, 16));
35          top.add(counter, BorderLayout.WEST);
```

Barra blu con il contatore.

**Righe 37–42**

```java
37          prev = Ui.button("◀ Precedente", () -> show(pos - 1));
38          next = Ui.button("Successiva ▶", () -> show(pos + 1));
39          JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
40          bottom.add(prev);
41          bottom.add(next);
42          bottom.add(Ui.button("Chiudi", this::dispose));
```

Pulsanti Precedente, Successiva e Chiudi.

**Righe 44–52**

```java
44          JPanel root = new JPanel(new BorderLayout());
45          root.add(top, BorderLayout.NORTH);
46          root.add(view, BorderLayout.CENTER);
47          root.add(bottom, BorderLayout.SOUTH);
48          setContentPane(root);
49          setSize(980, 760);
50          setLocationRelativeTo(owner);
51          show(startPos);
52      }
```

Composizione, dimensioni (980×760), posizione e prima domanda.

**Righe 54–66**

```java
54      private void show(int p) {
55          if (p < 0 || p >= indexes.size()) {
56              return;
57          }
58          pos = p;
59          int idx = indexes.get(p);
60          view.display(session.getQuestion(idx), session.getResponse(idx), false, true);
61          counter.setText("Domanda " + (idx + 1) + " dell'esame  ·  " + (p + 1) + " di " + indexes.size()
62              + " in revisione" + (session.isMarked(idx) ? "  ·  ⚑ marcata" : ""));
63          prev.setEnabled(p > 0);
64          next.setEnabled(p < indexes.size() - 1);
65      }
66  }
```

Mostra la posizione `p` (se valida): la domanda in sola lettura con correzione, il contatore con numero nell'esame, posizione nella revisione ed eventuale marcatura; abilita/disabilita i pulsanti agli estremi.
