package com.lordoc.ui;

import com.lordoc.model.Domain;
import com.lordoc.model.DomainScore;
import com.lordoc.model.ExamResult;
import com.lordoc.model.ExamSession;
import com.lordoc.model.Question;
import com.lordoc.util.Text;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Report finale dell'esame: punteggio, dettaglio per dominio, revisione domande. */
public final class ResultsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final String[] FILTERS = {"Tutte", "Solo errate / incomplete", "Solo corrette", "Solo marcate"};

    private final ExamResult result;
    private final ExamSession session;
    private final List<Integer> visible = new ArrayList<>();
    private final JComboBox<String> filter = new JComboBox<>(FILTERS);
    private final QuestionsModel questionsModel = new QuestionsModel();

    public ResultsPanel(ExamResult result, Runnable onHome, Consumer<List<Question>> onRetryWrong) {
        super(new BorderLayout());
        this.result = result;
        this.session = result.getSession();

        add(buildHeader(), BorderLayout.NORTH);

        JTable questions = new JTable(questionsModel);
        questions.setRowHeight(24);
        questions.setFont(Ui.BASE.deriveFont(14f));
        questions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        questions.getColumnModel().getColumn(0).setMaxWidth(50);
        questions.getColumnModel().getColumn(4).setMinWidth(120);
        questions.getColumnModel().getColumn(4).setMaxWidth(140);
        questions.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    int idx = visible.get(row);
                    boolean ok = session.getQuestion(idx).isCorrect(session.getResponse(idx));
                    c.setForeground(col == 4 ? (ok ? Ui.OK : Ui.KO) : Color.BLACK);
                }
                return c;
            }
        });
        questions.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && questions.getSelectedRow() >= 0) {
                    openReview(questions.getSelectedRow());
                }
            }
        });
        filter.addActionListener(e -> applyFilter());
        applyFilter();

        JPanel qPanel = new JPanel(new BorderLayout());
        qPanel.setBorder(Ui.titled("Domande (doppio clic per la revisione con spiegazione)"));
        JPanel qTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qTop.add(new JLabel("Mostra:"));
        qTop.add(filter);
        qTop.add(Ui.button("Rivedi selezionata", () -> openReview(Math.max(0, questions.getSelectedRow()))));
        qPanel.add(qTop, BorderLayout.NORTH);
        qPanel.add(new JScrollPane(questions), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildDomainPanel(), qPanel);
        split.setResizeWeight(0.42);
        split.setBorder(Ui.padding(8, 8));
        add(split, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(Ui.button("Ripassa errori in modalità Studio", () -> {
            List<Question> wrong = new ArrayList<>();
            for (int i = 0; i < session.size(); i++) {
                if (!session.getQuestion(i).isCorrect(session.getResponse(i))) {
                    wrong.add(session.getQuestion(i));
                }
            }
            if (wrong.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Nessun errore: complimenti!");
            } else {
                onRetryWrong.accept(wrong);
            }
        }));
        bottom.add(Ui.button("Torna al menu principale", onHome));
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout(20, 0));
        h.setBackground(Color.WHITE);
        h.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xC5CED9)), Ui.padding(14, 22)));

        JPanel left = new JPanel(new GridBagLayout());
        left.setOpaque(false);
        left.add(Ui.label(result.getTitle() + " — " + result.getMode().getLabel(), Ui.TITLE, Ui.HEADER_BG),
            Ui.row(0));
        boolean pass = result.isPassed();
        left.add(Ui.label(pass ? "SUPERATO" : "NON SUPERATO", Ui.TITLE, pass ? Ui.OK : Ui.KO), Ui.row(1));
        left.add(Ui.label(String.format("%d corrette su %d (%.1f%%)  ·  Tempo impiegato: %s",
            result.getCorrect(), result.getTotal(), result.getPercent(),
            Text.formatDuration(result.getDurationSeconds())), Ui.BASE, null), Ui.row(2));
        h.add(left, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JLabel score = Ui.label(result.getScaledScore() + " / " + ExamResult.MAX_SCORE, Ui.HUGE,
            pass ? Ui.OK : Ui.KO);
        score.setHorizontalAlignment(JLabel.CENTER);
        right.add(score, BorderLayout.NORTH);
        right.add(new ScoreBar(result.getScaledScore()), BorderLayout.CENTER);
        JLabel note = Ui.label("Punteggio stimato su scala 300-1000. Cisco non pubblica la soglia ufficiale: "
            + ExamResult.PASSING_SCORE + " è un obiettivo prudente.", Ui.SMALL, Color.GRAY);
        right.add(note, BorderLayout.SOUTH);
        h.add(right, BorderLayout.CENTER);
        return h;
    }

    private JPanel buildDomainPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(Ui.titled("Risultato per dominio (exam topic)"));
        int y = 0;
        for (Map.Entry<Domain, DomainScore> e : result.getByDomain().entrySet()) {
            DomainScore ds = e.getValue();
            p.add(Ui.label(e.getKey().getLabel() + "  (" + e.getKey().getWeight() + "% dell'esame)", Ui.BOLD, null),
                Ui.row(y++));
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue((int) Math.round(ds.getPercent()));
            bar.setStringPainted(true);
            bar.setString(ds.getCorrect() + " / " + ds.getTotal() + "  (" + Ui.pct(ds.getPercent()) + ")");
            bar.setForeground(ds.getPercent() >= 82.5 ? Ui.OK : ds.getPercent() >= 60 ? Ui.WARN : Ui.KO);
            p.add(bar, Ui.row(y++));
        }
        JLabel tip = Ui.label("<html><i>Verde ≥ 82,5% · Arancio ≥ 60% · Rosso &lt; 60%. "
            + "Concentra lo studio sui domini in rosso con un esame personalizzato.</i></html>", Ui.SMALL, Color.GRAY);
        p.add(tip, Ui.row(y++));
        Ui.addFiller(p, y);
        return p;
    }

    private void applyFilter() {
        visible.clear();
        int f = filter.getSelectedIndex();
        for (int i = 0; i < session.size(); i++) {
            boolean ok = session.getQuestion(i).isCorrect(session.getResponse(i));
            boolean include = f == 0 || (f == 1 && !ok) || (f == 2 && ok) || (f == 3 && session.isMarked(i));
            if (include) {
                visible.add(i);
            }
        }
        questionsModel.fireTableDataChanged();
    }

    private void openReview(int row) {
        if (visible.isEmpty()) {
            return;
        }
        new ReviewDialog(SwingUtilities.getWindowAncestor(this), session, new ArrayList<>(visible),
            Math.min(row, visible.size() - 1)).setVisible(true);
    }

    private final class QuestionsModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] cols = {"#", "ID", "Dominio", "Tipo", "Esito"};

        @Override public int getRowCount() { return visible.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            int idx = visible.get(r);
            Question q = session.getQuestion(idx);
            switch (c) {
                case 0: return idx + 1;
                case 1: return q.getId() + (session.isMarked(idx) ? " ⚑" : "");
                case 2: return q.getDomain().getLabel();
                case 3: return q.getType().getLabel();
                default:
                    if (q.isCorrect(session.getResponse(idx))) {
                        return "✔ Corretta";
                    }
                    return q.isAnswered(session.getResponse(idx)) ? "✘ Errata" : "✘ Mancante";
            }
        }
    }
}
