# Riga per riga — pacchetto `ui` (1/3): componenti di base e finestra principale

Questa parte copre gli "attrezzi" grafici condivisi (`Ui`, `WrapLabel`, `ScrollablePanel`), i due componenti disegnati a mano (`ScoreBar`, `ScoreChart`) e la finestra principale (`MainFrame`, `HomePanel`). Descrizione generale: [06 - ui](../06-classi-ui.md).

Nota ricorrente: ogni classe grafica contiene `private static final long serialVersionUID = 1L;`. I componenti Swing sono "serializzabili" (salvabili come byte) e Java raccomanda di dichiarare un numero di versione; nel programma non si usa questa funzione, la riga serve solo a evitare un avviso del compilatore.

**File in questo documento:** [`Ui.java`](#uijava) · [`WrapLabel.java`](#wraplabeljava) · [`ScrollablePanel.java`](#scrollablepaneljava) · [`ScoreBar.java`](#scorebarjava) · [`ScoreChart.java`](#scorechartjava) · [`MainFrame.java`](#mainframejava) · [`HomePanel.java`](#homepaneljava)

---

## Ui.java

`src/main/java/com/lordoc/ui/Ui.java` — colori, caratteri e funzioni per creare componenti nello stile del programma.

**Righe 1–13**

```java
 1  package com.lordoc.ui;
 2  
 3  import javax.swing.BorderFactory;
 4  import javax.swing.JButton;
 5  import javax.swing.JComponent;
 6  import javax.swing.JLabel;
 7  import javax.swing.JPanel;
 8  import javax.swing.border.Border;
 9  import java.awt.Color;
10  import java.awt.Component;
11  import java.awt.Font;
12  import java.awt.GridBagConstraints;
13  import java.awt.Insets;
```

Pacchetto e import dei componenti Swing e AWT usati (pulsanti, etichette, pannelli, bordi, colori, caratteri, vincoli GridBag).

**Righe 15–16**

```java
15  /** Costanti grafiche e piccoli helper Swing condivisi. */
16  public final class Ui {
```

Inizio della classe `Ui`.

**Righe 18–29**

```java
18      public static final Color HEADER_BG = new Color(0x1F3A5F);
19      public static final Color HEADER_FG = Color.WHITE;
20      public static final Color SUBHEADER_BG = new Color(0xE8EEF5);
21      public static final Color ACCENT = new Color(0x2E6DB4);
22      public static final Color OK = new Color(0x2E7D32);
23      public static final Color OK_BG = new Color(0xE3F4E4);
24      public static final Color KO = new Color(0xC62828);
25      public static final Color KO_BG = new Color(0xFBE4E4);
26      public static final Color WARN = new Color(0xE69500);
27      public static final Color EXHIBIT_BG = new Color(0x1E1E1E);
28      public static final Color EXHIBIT_FG = new Color(0xE6E6E6);
29      public static final Color PANEL_BG = Color.WHITE;
```

La **tavolozza** del programma. Ogni colore è scritto in esadecimale RGB (es. `0x1F3A5F`: rosso 1F, verde 3A, blu 5F). Intestazione blu scuro con testo bianco, sottointestazione azzurro chiaro, blu d'accento, verde e verde chiaro (giusto), rosso e rosa (sbagliato), arancio (avviso), nero e grigio chiaro per gli exhibit, bianco per i pannelli.

**Righe 31–36**

```java
31      public static final Font BASE = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
32      public static final Font BOLD = BASE.deriveFont(Font.BOLD);
33      public static final Font SMALL = BASE.deriveFont(13f);
34      public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
35      public static final Font HUGE = new Font(Font.SANS_SERIF, Font.BOLD, 44);
36      public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 14);
```

I **caratteri**: base a 15 punti, la sua versione in grassetto, una più piccola (13), il titolo (22 grassetto), quello enorme del punteggio (44) e il monospazio per exhibit e indirizzi. `deriveFont` crea una variante di un carattere esistente.

**Riga 38**

```java
38      private Ui() { }
```

Costruttore privato: la classe contiene solo costanti e funzioni statiche.

**Righe 40–46**

```java
40      public static JButton button(String text, Runnable action) {
41          JButton b = new JButton(text);
42          b.setFont(BOLD.deriveFont(14f));
43          b.setFocusPainted(false);
44          b.addActionListener(e -> action.run());
45          return b;
46      }
```

Crea un pulsante in grassetto, senza il rettangolo di focus disegnato sul testo, che al clic esegue l'azione ricevuta (`action.run()`).

**Righe 48–55**

```java
48      public static JLabel label(String text, Font font, Color fg) {
49          JLabel l = new JLabel(text);
50          l.setFont(font);
51          if (fg != null) {
52              l.setForeground(fg);
53          }
54          return l;
55      }
```

Crea un'etichetta con il carattere indicato e, se fornito, il colore del testo.

**Righe 57–59**

```java
57      public static Border padding(int v, int h) {
58          return BorderFactory.createEmptyBorder(v, h, v, h);
59      }
```

Bordo invisibile che crea margine: `v` pixel sopra/sotto, `h` a sinistra/destra.

**Righe 61–65**

```java
61      public static Border titled(String title) {
62          return BorderFactory.createCompoundBorder(
63              BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0xC5CED9)), title),
64              padding(6, 8));
65      }
```

Bordo composto: una cornice grigio-azzurra con titolo, più un margine interno.

**Righe 67–78**

```java
67      /** Pannello "header" scuro con titolo. */
68      public static JPanel header(String title, String subtitle) {
69          JPanel p = new JPanel(new java.awt.BorderLayout());
70          p.setBackground(HEADER_BG);
71          p.setBorder(padding(12, 18));
72          JLabel t = label(title, TITLE, HEADER_FG);
73          p.add(t, java.awt.BorderLayout.NORTH);
74          if (subtitle != null) {
75              p.add(label(subtitle, SMALL, new Color(0xC9D6E6)), java.awt.BorderLayout.SOUTH);
76          }
77          return p;
78      }
```

`header`: pannello blu con il titolo in alto (NORTH) e, se presente, il sottotitolo in basso (SOUTH) in azzurro chiaro.

**Righe 80–90**

```java
80      /** Vincoli GridBag per impilare componenti a tutta larghezza. */
81      public static GridBagConstraints row(int y) {
82          GridBagConstraints c = new GridBagConstraints();
83          c.gridx = 0;
84          c.gridy = y;
85          c.weightx = 1;
86          c.fill = GridBagConstraints.HORIZONTAL;
87          c.anchor = GridBagConstraints.NORTHWEST;
88          c.insets = new Insets(4, 0, 4, 0);
89          return c;
90      }
```

`row(y)`: prepara le "istruzioni" per il `GridBagLayout`: colonna 0, riga `y`, il componente prende tutto lo spazio orizzontale disponibile (`weightx = 1`, `fill = HORIZONTAL`), è ancorato in alto a sinistra e ha 4 pixel di spazio sopra e sotto.

**Righe 92–100**

```java
 92      /** Aggiunge un riempitivo verticale in fondo a un pannello GridBag. */
 93      public static void addFiller(JPanel p, int y) {
 94          GridBagConstraints c = row(y);
 95          c.weighty = 1;
 96          c.fill = GridBagConstraints.BOTH;
 97          JPanel filler = new JPanel();
 98          filler.setOpaque(false);
 99          p.add(filler, c);
100      }
```

`addFiller`: aggiunge un pannello trasparente che "assorbe" tutto lo spazio verticale avanzato (`weighty = 1`), spingendo il contenuto verso l'alto.

**Righe 102–109**

```java
102      public static void setAllFonts(JComponent root, Font f) {
103          for (Component c : root.getComponents()) {
104              c.setFont(f);
105              if (c instanceof JComponent) {
106                  setAllFonts((JComponent) c, f);
107              }
108          }
109      }
```

Imposta un carattere su un componente e, **ricorsivamente** (il metodo chiama sé stesso), su tutti quelli contenuti al suo interno.

**Righe 111–114**

```java
111      public static String pct(double v) {
112          return String.format("%.0f%%", v);
113      }
114  }
```

Formatta una percentuale senza decimali: `%.0f` è il numero arrotondato, `%%` scrive il simbolo %.

---

## WrapLabel.java

`src/main/java/com/lordoc/ui/WrapLabel.java` — etichetta multilinea con a capo automatico.

**Righe 1–5**

```java
1  package com.lordoc.ui;
2  
3  import javax.swing.JTextArea;
4  import java.awt.Color;
5  import java.awt.Font;
```

Pacchetto e import.

**Righe 7–9**

```java
7  /** Etichetta multilinea con a-capo automatico (basata su JTextArea non modificabile). */
8  public class WrapLabel extends JTextArea {
9      private static final long serialVersionUID = 1L;
```

La classe **estende** `JTextArea` (un'area di testo) e la trasforma in un'etichetta.

**Righe 11–23**

```java
11      public WrapLabel(String text, Font font) {
12          super(text);
13          setFont(font);
14          setLineWrap(true);
15          setWrapStyleWord(true);
16          setEditable(false);
17          setFocusable(false);
18          setOpaque(false);
19          setBorder(null);
20          setHighlighter(null);
21          setForeground(Color.BLACK);
22      }
23  }
```

Costruttore: imposta testo (con `super(text)`, cioè il costruttore di `JTextArea`) e carattere, attiva l'a capo per parole (`setLineWrap` + `setWrapStyleWord`), la rende non modificabile e non selezionabile (niente focus, niente evidenziazione), trasparente, senza bordo, con testo nero.

---

## ScrollablePanel.java

`src/main/java/com/lordoc/ui/ScrollablePanel.java` — pannello che segue la larghezza della finestra.

**Righe 1–7**

```java
1  package com.lordoc.ui;
2  
3  import javax.swing.JPanel;
4  import javax.swing.Scrollable;
5  import java.awt.Dimension;
6  import java.awt.LayoutManager;
7  import java.awt.Rectangle;
```

Pacchetto e import.

**Righe 9–11**

```java
 9  /** Pannello che segue la larghezza del viewport (così il testo va a capo) e scorre in verticale. */
10  public class ScrollablePanel extends JPanel implements Scrollable {
11      private static final long serialVersionUID = 1L;
```

`implements Scrollable`: la classe promette di fornire i metodi che una `JScrollPane` interroga per sapere come far scorrere il contenuto.

**Righe 13–15**

```java
13      public ScrollablePanel(LayoutManager layout) {
14          super(layout);
15      }
```

Costruttore: passa il layout scelto al pannello.

**Righe 17–18**

```java
17      @Override
18      public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
```

`getPreferredScrollableViewportSize`: la dimensione preferita dell'area visibile coincide con quella del pannello.

**Righe 20–21**

```java
20      @Override
21      public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 20; }
```

`getScrollableUnitIncrement`: ogni "scatto" (rotella o freccia) scorre di 20 pixel.

**Righe 23–26**

```java
23      @Override
24      public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
25          return Math.max(40, visibleRect.height - 40);
26      }
```

`getScrollableBlockIncrement`: un "salto di pagina" scorre quasi un'intera schermata (altezza visibile − 40, minimo 40).

**Righe 28–29**

```java
28      @Override
29      public boolean getScrollableTracksViewportWidth() { return true; }
```

`getScrollableTracksViewportWidth = true`: il pannello è sempre largo quanto la finestra visibile → niente scorrimento orizzontale e il testo va a capo.

**Righe 31–33**

```java
31      @Override
32      public boolean getScrollableTracksViewportHeight() { return false; }
33  }
```

`getScrollableTracksViewportHeight = false`: in altezza il pannello può crescere quanto vuole → compare la barra verticale.

---

## ScoreBar.java

`src/main/java/com/lordoc/ui/ScoreBar.java` — barra grafica del punteggio con la soglia.

**Righe 1–11**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.model.ExamResult;
 4  
 5  import javax.swing.JComponent;
 6  import java.awt.BasicStroke;
 7  import java.awt.Color;
 8  import java.awt.Dimension;
 9  import java.awt.Graphics;
10  import java.awt.Graphics2D;
11  import java.awt.RenderingHints;
```

Pacchetto e import delle classi di disegno (`Graphics2D`, colori, tratti).

**Righe 13–15**

```java
13  /** Barra orizzontale del punteggio (300-1000) con la soglia di superamento. */
14  public final class ScoreBar extends JComponent {
15      private static final long serialVersionUID = 1L;
```

`extends JComponent`: un componente "vuoto" che disegniamo noi.

**Riga 17**

```java
17      private final int score;
```

Il punteggio da rappresentare.

**Righe 19–22**

```java
19      public ScoreBar(int score) {
20          this.score = score;
21          setPreferredSize(new Dimension(500, 54));
22      }
```

Costruttore: salva il punteggio e chiede uno spazio di 500×54 pixel.

**Riga 24**

```java
24      @Override
```

`paintComponent` viene chiamato da Swing ogni volta che il componente va ridisegnato.

**Righe 25–33**

```java
25      protected void paintComponent(Graphics g0) {
26          Graphics2D g = (Graphics2D) g0.create();
27          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
28          int w = getWidth() - 20;
29          int x0 = 10;
30          int barY = 8;
31          int barH = 22;
32          int min = ExamResult.MIN_SCORE;
33          int max = ExamResult.MAX_SCORE;
```

Crea una copia del "pennello" (`create()`, così le modifiche non influenzano altri componenti) e attiva l'antialiasing per bordi morbidi. Calcola larghezza utile, margine, posizione e altezza della barra e i limiti della scala.

**Righe 34–35**

```java
34          g.setColor(new Color(0xE4E8EE));
35          g.fillRoundRect(x0, barY, w, barH, 10, 10);
```

Disegna lo sfondo grigio della barra (rettangolo con angoli arrotondati).

**Righe 36–38**

```java
36          int fill = (int) ((double) (score - min) / (max - min) * w);
37          g.setColor(score >= ExamResult.PASSING_SCORE ? Ui.OK : Ui.KO);
38          g.fillRoundRect(x0, barY, Math.max(fill, 6), barH, 10, 10);
```

Calcola quanta parte riempire in proporzione: (punteggio − 300) / 700 × larghezza. Colore verde se superato, rosso altrimenti; almeno 6 pixel per restare visibile anche con 300.

**Righe 39–42**

```java
39          int passX = x0 + (int) ((double) (ExamResult.PASSING_SCORE - min) / (max - min) * w);
40          g.setColor(Color.BLACK);
41          g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 3f}, 0f));
42          g.drawLine(passX, barY - 4, passX, barY + barH + 4);
```

Posizione orizzontale della soglia 825; disegna una linea verticale nera tratteggiata (l'array `{4f, 3f}` = 4 pixel pieni, 3 vuoti).

**Righe 43–50**

```java
43          g.setFont(Ui.SMALL);
44          String label = "Soglia " + ExamResult.PASSING_SCORE;
45          int lw = g.getFontMetrics().stringWidth(label);
46          g.drawString(label, Math.min(passX - lw / 2, x0 + w - lw), barY + barH + 18);
47          g.drawString(String.valueOf(min), x0, barY + barH + 18);
48          g.dispose();
49      }
50  }
```

Scrive "Soglia 825" centrata sotto la linea (senza uscire dal bordo destro: `Math.min`) e "300" all'inizio. `stringWidth` misura la larghezza del testo in pixel. `dispose()` libera la copia del pennello.

---

## ScoreChart.java

`src/main/java/com/lordoc/ui/ScoreChart.java` — grafico a linee dei punteggi nel tempo.

**Righe 1–13**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.model.ExamResult;
 4  
 5  import javax.swing.JComponent;
 6  import java.awt.BasicStroke;
 7  import java.awt.Color;
 8  import java.awt.Dimension;
 9  import java.awt.Graphics;
10  import java.awt.Graphics2D;
11  import java.awt.RenderingHints;
12  import java.util.ArrayList;
13  import java.util.List;
```

Pacchetto e import.

**Righe 15–17**

```java
15  /** Grafico a linee dell'andamento dei punteggi nel tempo. */
16  public final class ScoreChart extends JComponent {
17      private static final long serialVersionUID = 1L;
```

Inizio della classe (componente disegnato a mano).

**Riga 19**

```java
19      private List<ExamResult> results = new ArrayList<>();
```

I risultati da disegnare.

**Righe 21–23**

```java
21      public ScoreChart() {
22          setPreferredSize(new Dimension(400, 220));
23      }
```

Costruttore: dimensione preferita 400×220.

**Righe 25–29**

```java
25      public void setResults(List<ExamResult> list) {
26          int from = Math.max(0, list.size() - 30);
27          this.results = new ArrayList<>(list.subList(from, list.size()));
28          repaint();
29      }
```

Tiene solo gli ultimi 30 risultati (da `from` alla fine) e chiede di ridisegnare (`repaint`).

**Riga 31**

```java
31      @Override
```

Inizio di `paintComponent`.

**Righe 32–44**

```java
32      protected void paintComponent(Graphics g0) {
33          Graphics2D g = (Graphics2D) g0.create();
34          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
35          g.setColor(Color.WHITE);
36          g.fillRect(0, 0, getWidth(), getHeight());
37          int left = 46;
38          int right = 16;
39          int top = 16;
40          int bottom = 28;
41          int w = getWidth() - left - right;
42          int h = getHeight() - top - bottom;
43          int min = ExamResult.MIN_SCORE;
44          int max = ExamResult.MAX_SCORE;
```

Pennello con antialiasing, sfondo bianco, margini (sinistra più ampia per i numeri) e dimensioni dell'area del grafico; limiti della scala 300-1000.

**Righe 46–53**

```java
46          g.setFont(Ui.SMALL.deriveFont(11f));
47          for (int v = min; v <= max; v += 100) {
48              int y = top + h - (int) ((double) (v - min) / (max - min) * h);
49              g.setColor(new Color(0xEEEEEE));
50              g.drawLine(left, y, left + w, y);
51              g.setColor(Color.GRAY);
52              g.drawString(String.valueOf(v), 8, y + 4);
53          }
```

Griglia: per ogni valore da 300 a 1000 a passi di 100 calcola la coordinata verticale (in Swing la y cresce verso il **basso**, per questo si parte da `top + h` e si sottrae), disegna una linea grigio chiaro e scrive il valore a sinistra.

**Righe 54–58**

```java
54          int passY = top + h - (int) ((double) (ExamResult.PASSING_SCORE - min) / (max - min) * h);
55          g.setColor(Ui.OK);
56          g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {5f, 4f}, 0f));
57          g.drawLine(left, passY, left + w, passY);
58          g.drawString("soglia " + ExamResult.PASSING_SCORE, left + 4, passY - 4);
```

Linea tratteggiata verde della soglia con l'etichetta "soglia 825".

**Righe 60–66**

```java
60          if (results.isEmpty()) {
61              g.setColor(Color.GRAY);
62              g.setFont(Ui.BASE);
63              g.drawString("Nessun esame svolto: i tuoi progressi appariranno qui.", left + 20, top + h / 2);
64              g.dispose();
65              return;
66          }
```

Se non ci sono esami scrive un messaggio al centro e termina.

**Righe 67–83**

```java
67          g.setStroke(new BasicStroke(2.2f));
68          int n = results.size();
69          int prevX = -1;
70          int prevY = -1;
71          for (int i = 0; i < n; i++) {
72              int x = left + (n == 1 ? w / 2 : (int) ((double) i / (n - 1) * w));
73              int s = results.get(i).getScaledScore();
74              int y = top + h - (int) ((double) (s - min) / (max - min) * h);
75              if (prevX >= 0) {
76                  g.setColor(Ui.ACCENT);
77                  g.drawLine(prevX, prevY, x, y);
78              }
79              g.setColor(s >= ExamResult.PASSING_SCORE ? Ui.OK : Ui.KO);
80              g.fillOval(x - 4, y - 4, 8, 8);
81              prevX = x;
82              prevY = y;
83          }
```

Linea più spessa per la spezzata. Per ogni risultato calcola la x (distribuendo i punti su tutta la larghezza; con un solo punto lo mette al centro) e la y; se non è il primo punto traccia il segmento blu dal punto precedente; disegna un pallino verde o rosso. Ricorda il punto per il segmento successivo.

**Righe 84–88**

```java
84          g.setColor(Color.GRAY);
85          g.drawString("Ultimi " + n + " esami", left + w - 90, top + h + 20);
86          g.dispose();
87      }
88  }
```

Didascalia "Ultimi N esami" in basso a destra e rilascio del pennello.

---

## MainFrame.java

`src/main/java/com/lordoc/ui/MainFrame.java` — la finestra principale e la navigazione tra le schermate.

**Righe 1–21**

```java
 1  package com.lordoc.ui;
 2  
 3  import com.lordoc.app.AppContext;
 4  import com.lordoc.engine.Scorer;
 5  import com.lordoc.model.ExamMode;
 6  import com.lordoc.model.ExamResult;
 7  import com.lordoc.model.ExamSession;
 8  import com.lordoc.model.Question;
 9  
10  import javax.swing.JFrame;
11  import javax.swing.JMenu;
12  import javax.swing.JMenuBar;
13  import javax.swing.JMenuItem;
14  import javax.swing.JOptionPane;
15  import javax.swing.JPanel;
16  import javax.swing.WindowConstants;
17  import java.awt.CardLayout;
18  import java.awt.Dimension;
19  import java.awt.event.WindowAdapter;
20  import java.awt.event.WindowEvent;
21  import java.util.List;
```

Pacchetto e import.

**Righe 23–25**

```java
23  /** Finestra principale: alterna menu, esame in corso e risultati. */
24  public final class MainFrame extends JFrame {
25      private static final long serialVersionUID = 1L;
```

`extends JFrame`: è una finestra.

**Righe 27–32**

```java
27      private final AppContext ctx;
28      private final CardLayout cards = new CardLayout();
29      private final JPanel root = new JPanel(cards);
30      private final HomePanel home;
31      private ExamPanel examPanel;
32      private JPanel resultsPanel;
```

Campi: i servizi, il `CardLayout` (il "mazzo di carte"), il pannello `root` che lo usa, la home e i riferimenti all'esame e al report correnti (possono essere `null`).

**Righe 34–40**

```java
34      public MainFrame(AppContext ctx) {
35          super(AppContext.APP_NAME + " — Simulatore d'esame CCNA 200-301");
36          this.ctx = ctx;
37          this.home = new HomePanel(ctx, this::startExam);
38          root.add(home, "home");
39          setContentPane(root);
40          setJMenuBar(buildMenu());
```

Costruttore: titolo della finestra, creazione della home (le passa `this::startExam`, cioè "quando vuoi avviare un esame, chiama il mio `startExam`"), aggiunta come prima carta, menu.

**Righe 41–49**

```java
41          setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
42          addWindowListener(new WindowAdapter() {
43              @Override
44              public void windowClosing(WindowEvent e) { exit(); }
45          });
46          setMinimumSize(new Dimension(980, 680));
47          setSize(1240, 840);
48          setLocationRelativeTo(null);
49      }
```

La X della finestra non chiude direttamente: un *listener* intercetta la chiusura e chiama `exit()`, che chiede conferma se c'è un esame in corso. Imposta dimensioni minime e iniziali e centra la finestra sullo schermo (`setLocationRelativeTo(null)`).

**Righe 51–72**

```java
51      private JMenuBar buildMenu() {
52          JMenuBar bar = new JMenuBar();
53          JMenu file = new JMenu("File");
54          JMenuItem home = new JMenuItem("Menu principale");
55          home.addActionListener(e -> {
56              if (examPanel == null || confirmAbort()) {
57                  showHome();
58              }
59          });
60          JMenuItem dataDir = new JMenuItem("Cartella dati utente...");
61          dataDir.addActionListener(e -> JOptionPane.showMessageDialog(this,
62              "Storico e statistiche sono salvati in:\n" + ctx.getStore().getDirectory()
63                  + "\n\nPuoi aggiungere le tue domande (file .txt) in:\n"
64                  + ctx.getStore().getUserQuestionsDirectory()
65                  + "\n\nIl formato è descritto nel README del progetto.",
66              "Cartella dati", JOptionPane.INFORMATION_MESSAGE));
67          JMenuItem exit = new JMenuItem("Esci");
68          exit.addActionListener(e -> exit());
69          file.add(home);
70          file.add(dataDir);
71          file.addSeparator();
72          file.add(exit);
```

Costruisce la barra dei menu. **File**: "Menu principale" (torna alla home, con conferma se c'è un esame), "Cartella dati utente…" (messaggio con i percorsi), separatore, "Esci".

**Righe 74–91**

```java
74          JMenu help = new JMenu("Aiuto");
75          JMenuItem guide = new JMenuItem("Guida rapida");
76          guide.addActionListener(e -> JOptionPane.showMessageDialog(this, HELP, "Guida rapida",
77              JOptionPane.INFORMATION_MESSAGE));
78          JMenuItem about = new JMenuItem("Informazioni");
79          about.addActionListener(e -> JOptionPane.showMessageDialog(this,
80              AppContext.APP_NAME + " " + AppContext.VERSION + "\n"
81                  + "Simulatore d'esame open source per la preparazione al Cisco CCNA 200-301.\n"
82                  + "Domande nella banca: " + ctx.getBank().size() + "\n\n"
83                  + "Progetto indipendente, non affiliato a Cisco Systems.\n"
84                  + "CCNA è un marchio registrato di Cisco Systems, Inc.",
85              "Informazioni", JOptionPane.INFORMATION_MESSAGE));
86          help.add(guide);
87          help.add(about);
88          bar.add(file);
89          bar.add(help);
90          return bar;
91      }
```

Menu **Aiuto**: "Guida rapida" (mostra il testo `HELP`) e "Informazioni" (nome, versione, numero di domande, nota sui marchi).

**Righe 93–108**

```java
 93      private static final String HELP =
 94          "MODALITÀ\n"
 95          + "• Simulazione: tempo limite (~1,2 min/domanda), nessuna spiegazione fino alla consegna.\n"
 96          + "• Studio: senza limite di tempo; 'Mostra risposta' (Ctrl+R) rivela correzione e spiegazione.\n\n"
 97          + "DURANTE L'ESAME\n"
 98          + "• Alt+→ / Alt+← : domanda successiva / precedente\n"
 99          + "• Ctrl+M : marca la domanda per rivederla\n"
100          + "• 'Riepilogo domande' mostra risposte incomplete e marcate.\n"
101          + "• Se disattivi 'consenti di tornare indietro' l'esame si comporta come quello reale Cisco.\n\n"
102          + "DOPO L'ESAME\n"
103          + "• Il report mostra il punteggio (scala 300-1000) e il dettaglio per dominio.\n"
104          + "• Doppio clic su una domanda per rivederla con la spiegazione.\n"
105          + "• 'Ripassa errori' crea subito un esame Studio con le sole domande sbagliate.\n\n"
106          + "METODO CONSIGLIATO\n"
107          + "1) Studio per argomento  2) Simulazione completa  3) Ripasso degli errori\n"
108          + "4) Ripeti finché non superi stabilmente 900 in simulazione.";
```

Il testo della guida rapida: modalità, scorciatoie, report e metodo consigliato. I pezzi sono uniti con `+`; `\n` è l'a capo.

**Riga 110**

```java
110      // ------------------------------------------------------------------ flussi
```

Separatore: i metodi che spostano l'utente tra le schermate (vedi [algoritmo 10](../08-algoritmi.md#10-navigazione-tra-le-schermate-mainframe)).

**Righe 112–119**

```java
112      public void startExam(ExamSession session) {
113          if (session == null) {
114              return;
115          }
116          examPanel = new ExamPanel(session, this::finishExam);
117          root.add(examPanel, "exam");
118          cards.show(root, "exam");
119      }
```

Se la sessione è `null` (esame vuoto) non fa nulla. Altrimenti crea il pannello d'esame passando `this::finishExam` come azione di consegna, lo aggiunge al mazzo con il nome "exam" e lo mostra.

**Righe 121–135**

```java
121      private void finishExam(ExamSession session) {
122          ExamResult result = Scorer.score(session);
123          ctx.getStore().recordResult(result);
124          if (examPanel != null) {
125              root.remove(examPanel);
126              examPanel = null;
127          }
128          if (resultsPanel != null) {
129              root.remove(resultsPanel);
130          }
131          resultsPanel = new ResultsPanel(result, this::showHome, this::retryWrong);
132          root.add(resultsPanel, "results");
133          cards.show(root, "results");
134          home.refresh();
135      }
```

Consegna: corregge, salva, rimuove la carta dell'esame, sostituisce l'eventuale report precedente con uno nuovo, lo mostra e aggiorna la home.

**Righe 137–139**

```java
137      private void retryWrong(List<Question> wrong) {
138          startExam(ctx.getFactory().fromQuestions("Ripasso errori", ExamMode.STUDY, wrong));
139      }
```

Avvia un esame Studio "Ripasso errori" con le domande indicate.

**Righe 141–149**

```java
141      private void showHome() {
142          if (examPanel != null) {
143              examPanel.abort();
144              root.remove(examPanel);
145              examPanel = null;
146          }
147          home.refresh();
148          cards.show(root, "home");
149      }
```

Torna alla home: se c'è un esame lo interrompe (`abort` ferma il timer) e lo rimuove; aggiorna e mostra la home.

**Righe 151–156**

```java
151      private boolean confirmAbort() {
152          int c = JOptionPane.showConfirmDialog(this,
153              "C'è un esame in corso: abbandonandolo le risposte andranno perse. Continuare?",
154              "Esame in corso", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
155          return c == JOptionPane.YES_OPTION;
156      }
```

Finestra di conferma Sì/No; restituisce vero se l'utente accetta di abbandonare.

**Righe 158–165**

```java
158      private void exit() {
159          if (examPanel != null && !confirmAbort()) {
160              return;
161          }
162          dispose();
163          System.exit(0);
164      }
165  }
```

Se c'è un esame e l'utente non conferma, non fa nulla; altrimenti chiude la finestra (`dispose`) e termina il programma.

---

## HomePanel.java

`src/main/java/com/lordoc/ui/HomePanel.java` — il menu principale a schede.

**Righe 1–9**

```java
1  package com.lordoc.ui;
2  
3  import com.lordoc.app.AppContext;
4  import com.lordoc.model.ExamSession;
5  
6  import javax.swing.JPanel;
7  import javax.swing.JTabbedPane;
8  import java.awt.BorderLayout;
9  import java.util.function.Consumer;
```

Pacchetto e import.

**Righe 11–13**

```java
11  /** Menu principale a schede. */
12  public final class HomePanel extends JPanel {
13      private static final long serialVersionUID = 1L;
```

Inizio della classe.

**Righe 15–16**

```java
15      private final ExamsPanel exams;
16      private final HistoryPanel history;
```

Riferimenti alle due schede che vanno aggiornate dopo ogni esame.

**Righe 18–31**

```java
18      public HomePanel(AppContext ctx, Consumer<ExamSession> starter) {
19          super(new BorderLayout());
20          exams = new ExamsPanel(ctx, starter);
21          history = new HistoryPanel(ctx);
22          add(Ui.header(AppContext.APP_NAME, "Simulatore d'esame per Cisco CCNA 200-301  ·  "
23              + ctx.getBank().size() + " domande in banca + generatore illimitato di subnetting"), BorderLayout.NORTH);
24          JTabbedPane tabs = new JTabbedPane();
25          tabs.setFont(Ui.BOLD.deriveFont(14f));
26          tabs.addTab("Esami", exams);
27          tabs.addTab("Storico e progressi", history);
28          tabs.addTab("Cheat sheet", new ReferencePanel());
29          tabs.addTab("Calcolatore subnet", new SubnetCalculatorPanel());
30          add(tabs, BorderLayout.CENTER);
31      }
```

Costruttore: crea le schede Esami e Storico; in alto l'intestazione con nome e numero di domande; al centro un `JTabbedPane` con quattro schede (le ultime due non hanno bisogno di aggiornamenti e vengono create al volo).

**Righe 33–37**

```java
33      public void refresh() {
34          exams.refresh();
35          history.refresh();
36      }
37  }
```

Aggiorna le tabelle degli esami e lo storico.
