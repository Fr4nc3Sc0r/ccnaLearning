package com.ccnaexamlab.ui;

import com.ccnaexamlab.app.AppContext;
import com.ccnaexamlab.data.QuestionStat;
import com.ccnaexamlab.engine.ExamConfig;
import com.ccnaexamlab.engine.ExamFactory;
import com.ccnaexamlab.engine.PresetExam;
import com.ccnaexamlab.engine.QuestionFilter;
import com.ccnaexamlab.model.Domain;
import com.ccnaexamlab.model.ExamMode;
import com.ccnaexamlab.model.ExamResult;
import com.ccnaexamlab.model.ExamSession;
import com.ccnaexamlab.model.Question;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

/** Scheda "Esami": esami predefiniti e modalità alternative. */
public final class ExamsPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final Consumer<ExamSession> starter;
    private final List<PresetExam> presets;
    private final PresetModel presetModel = new PresetModel();
    private final DomainModel domainModel = new DomainModel();
    private final JTable presetTable;
    private final JCheckBox allowBack = new JCheckBox(
        "Consenti di tornare alle domande precedenti (l'esame Cisco reale NON lo consente)", true);

    public ExamsPanel(AppContext ctx, Consumer<ExamSession> starter) {
        super(new BorderLayout());
        this.ctx = ctx;
        this.starter = starter;
        this.presets = ctx.getFactory().presetExams();

        // --- esami predefiniti
        presetTable = new JTable(presetModel);
        presetTable.setRowHeight(28);
        presetTable.setFont(Ui.BASE);
        presetTable.getTableHeader().setFont(Ui.BOLD.deriveFont(13f));
        presetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (!presets.isEmpty()) {
            presetTable.setRowSelectionInterval(0, 0);
        }
        JPanel presetPanel = new JPanel(new BorderLayout(0, 6));
        presetPanel.setBorder(Ui.titled("Esami predefiniti"));
        presetPanel.add(new WrapLabel("Ogni esame contiene un set fisso di domande distribuite secondo i pesi "
            + "del blueprint ufficiale. Svolgilo prima in Studio, poi in Simulazione per misurarti.", Ui.SMALL),
            BorderLayout.NORTH);
        presetPanel.add(new JScrollPane(presetTable), BorderLayout.CENTER);
        JPanel presetButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton sim = Ui.button("▶ Avvia in Simulazione", () -> startPreset(ExamMode.SIMULATION));
        sim.setForeground(Ui.ACCENT);
        presetButtons.add(sim);
        presetButtons.add(Ui.button("Avvia in Studio", () -> startPreset(ExamMode.STUDY)));
        JPanel southPreset = new JPanel(new BorderLayout());
        southPreset.add(presetButtons, BorderLayout.NORTH);
        allowBack.setFont(Ui.SMALL);
        southPreset.add(allowBack, BorderLayout.SOUTH);
        presetPanel.add(southPreset, BorderLayout.SOUTH);

        // --- altre modalità
        JPanel other = new JPanel(new GridBagLayout());
        other.setBorder(Ui.titled("Altre modalità"));
        int y = 0;
        y = addMode(other, y, "Esame completo casuale",
            "Domande scelte a caso da tutta la banca con i pesi ufficiali dei domini, a tempo.",
            this::startFullRandom);
        y = addMode(other, y, "Esame personalizzato…",
            "Scegli domini, numero di domande, filtri (mai viste, sbagliate, marcate), modalità e tempo.",
            this::startCustom);
        y = addMode(other, y, "Drill di subnetting",
            "20 esercizi di subnetting IPv4 generati al momento, con spiegazione passo-passo.",
            this::startSubnetDrill);
        y = addMode(other, y, "Ripassa gli errori",
            "Studio sulle domande che hai sbagliato l'ultima volta che le hai viste.",
            () -> startFiltered(QuestionFilter.LAST_WRONG, "Ripasso errori"));
        y = addMode(other, y, "Ripassa le marcate",
            "Studio sulle domande che hai marcato durante gli esami.",
            () -> startFiltered(QuestionFilter.FLAGGED, "Ripasso domande marcate"));

        JTable domains = new JTable(domainModel);
        domains.setRowHeight(22);
        domains.setFont(Ui.SMALL);
        domains.setEnabled(false);
        domains.getColumnModel().getColumn(0).setPreferredWidth(230);
        for (int i = 1; i < 5; i++) {
            domains.getColumnModel().getColumn(i).setPreferredWidth(70);
        }
        JScrollPane dsp = new JScrollPane(domains);
        dsp.setPreferredSize(new java.awt.Dimension(300, 175));
        JPanel dp = new JPanel(new BorderLayout());
        dp.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        dp.add(Ui.label("Copertura e accuratezza per dominio", Ui.BOLD, Ui.HEADER_BG), BorderLayout.NORTH);
        dp.add(dsp, BorderLayout.CENTER);
        other.add(dp, Ui.row(y++));
        Ui.addFiller(other, y);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, presetPanel, new JScrollPane(other));
        split.setResizeWeight(0.55);
        split.setBorder(Ui.padding(8, 8));
        add(split, BorderLayout.CENTER);
    }

    private int addMode(JPanel p, int y, String title, String desc, Runnable action) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 6, 0));
        JButton b = Ui.button(title, action);
        b.setPreferredSize(new java.awt.Dimension(230, 34));
        row.add(b, BorderLayout.WEST);
        WrapLabel l = new WrapLabel(desc, Ui.SMALL);
        l.setForeground(new Color(0x445566));
        row.add(l, BorderLayout.CENTER);
        p.add(row, Ui.row(y));
        return y + 1;
    }

    public void refresh() {
        presetModel.fireTableDataChanged();
        domainModel.fireTableDataChanged();
    }

    // ------------------------------------------------------------------ azioni

    private void startPreset(ExamMode mode) {
        int row = presetTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Seleziona un esame dalla tabella.");
            return;
        }
        PresetExam p = presets.get(row);
        boolean back = allowBack.isSelected() || mode == ExamMode.STUDY;
        if (mode == ExamMode.SIMULATION) {
            int c = JOptionPane.showConfirmDialog(this,
                p.getName() + ": " + p.size() + " domande in " + ExamFactory.suggestedMinutes(p.size())
                    + " minuti.\nIl cronometro parte subito. Pronto?",
                "Avvio simulazione", JOptionPane.OK_CANCEL_OPTION);
            if (c != JOptionPane.OK_OPTION) {
                return;
            }
        }
        starter.accept(ctx.getFactory().fromPreset(p, mode, back));
    }

    private void startFullRandom() {
        int n = Math.min(100, ctx.getBank().size());
        ExamConfig cfg = new ExamConfig()
            .title("Esame completo casuale")
            .mode(ExamMode.SIMULATION)
            .questionCount(n)
            .timeMinutes(ExamFactory.suggestedMinutes(n))
            .allowBack(allowBack.isSelected());
        starter.accept(ctx.getFactory().custom(cfg));
    }

    private void startCustom() {
        CustomExamDialog d = new CustomExamDialog(javax.swing.SwingUtilities.getWindowAncestor(this));
        d.setVisible(true);
        ExamConfig cfg = d.getResult();
        if (cfg == null) {
            return;
        }
        ExamSession s = ctx.getFactory().custom(cfg);
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Nessuna domanda corrisponde ai criteri scelti.",
                "Esame vuoto", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        starter.accept(s);
    }

    private void startSubnetDrill() {
        ExamConfig cfg = new ExamConfig()
            .title("Drill di subnetting")
            .mode(ExamMode.STUDY)
            .domains(EnumSet.noneOf(Domain.class))
            .questionCount(0)
            .subnetQuestions(20);
        starter.accept(ctx.getFactory().custom(cfg));
    }

    private void startFiltered(QuestionFilter f, String title) {
        ExamConfig cfg = new ExamConfig().title(title).mode(ExamMode.STUDY).filter(f)
            .questionCount(Integer.MAX_VALUE).weighted(false);
        ExamSession s = ctx.getFactory().custom(cfg);
        if (s == null) {
            JOptionPane.showMessageDialog(this, "Non ci sono domande da ripassare per ora.\n"
                + "Svolgi qualche esame: errori e domande marcate verranno raccolti qui.");
            return;
        }
        starter.accept(s);
    }

    // ------------------------------------------------------------------ modelli tabelle

    private final class PresetModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] cols = {"Esame", "Domande", "Tempo", "Tentativi", "Miglior punteggio", "Ultimo"};

        @Override public int getRowCount() { return presets.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            PresetExam p = presets.get(r);
            int attempts = 0;
            int best = -1;
            int last = -1;
            for (ExamResult res : ctx.getStore().getHistory()) {
                if (res.getTitle().equals(p.getName())) {
                    attempts++;
                    best = Math.max(best, res.getScaledScore());
                    last = res.getScaledScore();
                }
            }
            switch (c) {
                case 0: return p.getName();
                case 1: return p.size();
                case 2: return ExamFactory.suggestedMinutes(p.size()) + " min";
                case 3: return attempts;
                case 4: return best < 0 ? "—" : best + (best >= ExamResult.PASSING_SCORE ? " ✔" : "");
                default: return last < 0 ? "—" : String.valueOf(last);
            }
        }
    }

    private final class DomainModel extends AbstractTableModel {
        private static final long serialVersionUID = 1L;
        private final String[] cols = {"Dominio", "Peso", "Domande", "Viste", "Accuratezza"};

        @Override public int getRowCount() { return Domain.values().length; }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Domain d = Domain.values()[r];
            List<Question> qs = ctx.getBank().byDomain(d);
            int seenQuestions = 0;
            int answers = 0;
            int correct = 0;
            for (Question q : qs) {
                QuestionStat s = ctx.getStore().peekStat(q.getId());
                if (s != null && s.getSeen() > 0) {
                    seenQuestions++;
                    answers += s.getSeen();
                    correct += s.getCorrect();
                }
            }
            switch (c) {
                case 0: return d.getLabel();
                case 1: return d.getWeight() + "%";
                case 2: return qs.size();
                case 3: return seenQuestions;
                default: return answers == 0 ? "—" : Ui.pct(100.0 * correct / answers);
            }
        }
    }
}
