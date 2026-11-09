package com.lordoc.ui;

import com.lordoc.app.AppContext;
import com.lordoc.model.ExamSession;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.util.function.Consumer;

/** Menu principale a schede. */
public final class HomePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ExamsPanel exams;
    private final HistoryPanel history;

    public HomePanel(AppContext ctx, Consumer<ExamSession> starter) {
        super(new BorderLayout());
        exams = new ExamsPanel(ctx, starter);
        history = new HistoryPanel(ctx);
        add(Ui.header(AppContext.APP_NAME, "Simulatore d'esame per Cisco CCNA 200-301  ·  "
            + ctx.getBank().size() + " domande in banca + generatore illimitato di subnetting"), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Ui.BOLD.deriveFont(14f));
        tabs.addTab("Esami", exams);
        tabs.addTab("Storico e progressi", history);
        tabs.addTab("Cheat sheet", new ReferencePanel());
        tabs.addTab("Calcolatore subnet", new SubnetCalculatorPanel());
        add(tabs, BorderLayout.CENTER);
    }

    public void refresh() {
        exams.refresh();
        history.refresh();
    }
}
