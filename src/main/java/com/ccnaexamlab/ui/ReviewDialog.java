package com.ccnaexamlab.ui;

import com.ccnaexamlab.model.ExamSession;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.List;

/** Revisione post-esame: mostra domanda, risposta data, risposta corretta e spiegazione. */
public final class ReviewDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final ExamSession session;
    private final List<Integer> indexes;
    private final QuestionView view = new QuestionView();
    private final JLabel counter = Ui.label("", Ui.BOLD, Ui.HEADER_FG);
    private final JButton prev;
    private final JButton next;
    private int pos;

    public ReviewDialog(Window owner, ExamSession session, List<Integer> indexes, int startPos) {
        super(owner, "Revisione domande", ModalityType.APPLICATION_MODAL);
        this.session = session;
        this.indexes = indexes;
        this.pos = startPos;

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Ui.HEADER_BG);
        top.setBorder(Ui.padding(10, 16));
        top.add(counter, BorderLayout.WEST);

        prev = Ui.button("◀ Precedente", () -> show(pos - 1));
        next = Ui.button("Successiva ▶", () -> show(pos + 1));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(prev);
        bottom.add(next);
        bottom.add(Ui.button("Chiudi", this::dispose));

        JPanel root = new JPanel(new BorderLayout());
        root.add(top, BorderLayout.NORTH);
        root.add(view, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(980, 760);
        setLocationRelativeTo(owner);
        show(startPos);
    }

    private void show(int p) {
        if (p < 0 || p >= indexes.size()) {
            return;
        }
        pos = p;
        int idx = indexes.get(p);
        view.display(session.getQuestion(idx), session.getResponse(idx), false, true);
        counter.setText("Domanda " + (idx + 1) + " dell'esame  ·  " + (p + 1) + " di " + indexes.size()
            + " in revisione" + (session.isMarked(idx) ? "  ·  ⚑ marcata" : ""));
        prev.setEnabled(p > 0);
        next.setEnabled(p < indexes.size() - 1);
    }
}
