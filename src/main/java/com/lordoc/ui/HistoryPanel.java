package com.lordoc.ui;

import com.lordoc.app.AppContext;
import com.lordoc.model.Domain;
import com.lordoc.model.DomainScore;
import com.lordoc.model.ExamResult;
import com.lordoc.util.Text;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.time.format.DateTimeFormatter;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Scheda "Storico e progressi": elenco esami, grafico punteggi, aggregato per dominio. */
public final class HistoryPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AppContext ctx;
    private final HistoryModel model = new HistoryModel();
    private final ScoreChart chart = new ScoreChart();
    private final JPanel domainPanel = new JPanel(new GridBagLayout());

    public HistoryPanel(AppContext ctx) {
        super(new BorderLayout());
        this.ctx = ctx;

        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(Ui.BASE.deriveFont(14f));
        table.setAutoCreateRowSorter(true);
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(Ui.titled("Esami svolti"));
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clear = Ui.button("Azzera storico e statistiche", this::clear);
        clear.setForeground(Ui.KO);
        buttons.add(clear);
        tablePanel.add(buttons, BorderLayout.SOUTH);

        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(Ui.titled("Andamento dei punteggi"));
        chartPanel.add(chart, BorderLayout.CENTER);

        domainPanel.setBorder(Ui.titled("Accuratezza complessiva per dominio"));

        JSplitPane right = new JSplitPane(JSplitPane.VERTICAL_SPLIT, chartPanel, new JScrollPane(domainPanel));
        right.setResizeWeight(0.5);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePanel, right);
        split.setResizeWeight(0.55);
        split.setBorder(Ui.padding(8, 8));
        add(split, BorderLayout.CENTER);
        refresh();
    }

    public void refresh() {
        List<ExamResult> history = ctx.getStore().getHistory();
        model.fireTableDataChanged();
        chart.setResults(history);

        Map<Domain, DomainScore> agg = new EnumMap<>(Domain.class);
        for (ExamResult r : history) {
            r.getByDomain().forEach((d, s) -> agg.computeIfAbsent(d, k -> new DomainScore()).add(s));
        }
        domainPanel.removeAll();
        int y = 0;
        for (Domain d : Domain.values()) {
            DomainScore s = agg.get(d);
            domainPanel.add(Ui.label(d.getLabel(), Ui.BOLD.deriveFont(13f), null), Ui.row(y++));
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setStringPainted(true);
            if (s == null || s.getTotal() == 0) {
                bar.setString("nessun dato");
            } else {
                bar.setValue((int) Math.round(s.getPercent()));
                bar.setString(s.getCorrect() + " / " + s.getTotal() + "  (" + Ui.pct(s.getPercent()) + ")");
                bar.setForeground(s.getPercent() >= 82.5 ? Ui.OK : s.getPercent() >= 60 ? Ui.WARN : Ui.KO);
            }
            domainPanel.add(bar, Ui.row(y++));
        }
        Ui.addFiller(domainPanel, y);
        domainPanel.revalidate();
        domainPanel.repaint();
    }

    private void clear() {
        int c = JOptionPane.showConfirmDialog(this,
            "Cancellare definitivamente storico esami e statistiche delle domande?", "Conferma",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            ctx.getStore().clearHistory();
            refresh();
        }
    }

    private final class HistoryModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] cols = {"Data", "Esame", "Modalità", "Corrette", "Punteggio", "Esito", "Durata"};

        @Override public int getRowCount() { return ctx.getStore().getHistory().size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Class<?> getColumnClass(int c) { return c == 4 ? Integer.class : String.class; }

        @Override
        public Object getValueAt(int r, int c) {
            List<ExamResult> h = ctx.getStore().getHistory();
            ExamResult res = h.get(h.size() - 1 - r); // più recenti in alto
            switch (c) {
                case 0: return res.getDate().format(FMT);
                case 1: return res.getTitle();
                case 2: return res.getMode().getLabel();
                case 3: return res.getCorrect() + "/" + res.getTotal();
                case 4: return res.getScaledScore();
                case 5: return res.isPassed() ? "✔ Superato" : "✘ Non superato";
                default: return Text.formatDuration(res.getDurationSeconds());
            }
        }
    }
}
