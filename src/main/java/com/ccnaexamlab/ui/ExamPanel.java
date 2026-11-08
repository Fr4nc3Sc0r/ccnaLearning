package com.ccnaexamlab.ui;

import com.ccnaexamlab.model.ExamMode;
import com.ccnaexamlab.model.ExamSession;
import com.ccnaexamlab.model.Question;
import com.ccnaexamlab.util.Text;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

/** Schermata di svolgimento dell'esame. */
public final class ExamPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ExamSession session;
    private final Consumer<ExamSession> onFinish;
    private final QuestionView view = new QuestionView();
    private final JLabel counterLabel = Ui.label("", Ui.BOLD.deriveFont(17f), Ui.HEADER_FG);
    private final JLabel timerLabel = Ui.label("", Ui.MONO.deriveFont(java.awt.Font.BOLD, 20f), Ui.HEADER_FG);
    private final JLabel infoLabel = Ui.label("", Ui.SMALL, new Color(0x33475B));
    private final JCheckBox markBox = new JCheckBox("Marca per revisione (Ctrl+M)");
    private final JProgressBar progress = new JProgressBar();
    private final JButton prevBtn;
    private final JButton nextBtn;
    private final JButton reviewBtn;
    private final JButton showAnswerBtn;
    private final Timer timer;
    private boolean finished;

    public ExamPanel(ExamSession session, Consumer<ExamSession> onFinish) {
        super(new BorderLayout());
        this.session = session;
        this.onFinish = onFinish;

        // --- barra superiore
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Ui.HEADER_BG);
        top.setBorder(Ui.padding(10, 18));
        JLabel title = Ui.label(session.getTitle() + "  —  " + session.getMode().getLabel(), Ui.BOLD.deriveFont(17f),
            Ui.HEADER_FG);
        top.add(title, BorderLayout.WEST);
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        center.setOpaque(false);
        center.add(counterLabel);
        top.add(center, BorderLayout.CENTER);
        top.add(timerLabel, BorderLayout.EAST);

        JPanel sub = new JPanel(new BorderLayout());
        sub.setBackground(Ui.SUBHEADER_BG);
        sub.setBorder(Ui.padding(6, 18));
        markBox.setOpaque(false);
        markBox.setFont(Ui.BOLD.deriveFont(13f));
        markBox.addActionListener(e -> session.setMarked(session.getCurrent(), markBox.isSelected()));
        sub.add(markBox, BorderLayout.WEST);
        sub.add(infoLabel, BorderLayout.EAST);
        progress.setMaximum(session.size());
        progress.setStringPainted(true);
        progress.setBorder(BorderFactory.createEmptyBorder(2, 30, 2, 30));
        progress.setOpaque(false);
        sub.add(progress, BorderLayout.CENTER);

        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(top);
        north.add(sub);
        add(north, BorderLayout.NORTH);

        // --- domanda
        view.setOnChange(this::refreshStatus);
        add(view, BorderLayout.CENTER);

        // --- barra inferiore
        prevBtn = Ui.button("◀ Precedente", () -> go(session.getCurrent() - 1));
        nextBtn = Ui.button("Successiva ▶", this::next);
        reviewBtn = Ui.button("Riepilogo domande", this::openItemReview);
        showAnswerBtn = Ui.button("Mostra risposta", this::revealCurrent);
        JButton endBtn = Ui.button("Termina esame", this::confirmEnd);
        endBtn.setForeground(Ui.KO);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0xC5CED9)), Ui.padding(8, 14)));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.add(endBtn);
        left.add(reviewBtn);
        if (session.getMode() == ExamMode.STUDY) {
            left.add(showAnswerBtn);
        }
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.add(prevBtn);
        right.add(nextBtn);
        bottom.add(left, BorderLayout.WEST);
        bottom.add(right, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        if (!session.isAllowBack()) {
            prevBtn.setVisible(false);
            reviewBtn.setVisible(false);
        }

        installKeys();

        timer = new Timer(1000, e -> tick());
        timer.start();
        updateTimer();
        go(0);
    }

    private void installKeys() {
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), "next", this::next);
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), "prev", () -> {
            if (prevBtn.isVisible() && prevBtn.isEnabled()) {
                go(session.getCurrent() - 1);
            }
        });
        bind(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK), "mark", () -> {
            markBox.setSelected(!markBox.isSelected());
            session.setMarked(session.getCurrent(), markBox.isSelected());
        });
        if (session.getMode() == ExamMode.STUDY) {
            bind(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), "reveal", this::revealCurrent);
        }
    }

    private void bind(KeyStroke ks, String name, Runnable r) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, name);
        getActionMap().put(name, new AbstractAction() {
            private static final long serialVersionUID = 1L;
            @Override
            public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    // ------------------------------------------------------------------ navigazione

    private void go(int index) {
        if (index < 0 || index >= session.size()) {
            return;
        }
        session.setCurrent(index);
        Question q = session.getQuestion(index);
        boolean revealed = session.isRevealed(index);
        view.display(q, session.getResponse(index), !revealed, revealed);
        markBox.setSelected(session.isMarked(index));
        counterLabel.setText("Domanda " + (index + 1) + " di " + session.size());
        prevBtn.setEnabled(index > 0);
        nextBtn.setText(index == session.size() - 1 ? "Fine ▶" : "Successiva ▶");
        showAnswerBtn.setEnabled(!revealed);
        refreshStatus();
    }

    private void next() {
        int cur = session.getCurrent();
        if (!session.isAllowBack() && !session.isAnswered(cur)) {
            int c = JOptionPane.showConfirmDialog(this,
                "Non hai risposto completamente a questa domanda.\n"
                    + "In questa modalità non potrai tornare indietro. Proseguire?",
                "Domanda senza risposta", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (c != JOptionPane.YES_OPTION) {
                return;
            }
        }
        if (cur == session.size() - 1) {
            confirmEnd();
        } else {
            go(cur + 1);
        }
    }

    private void revealCurrent() {
        int cur = session.getCurrent();
        if (session.getMode() != ExamMode.STUDY || session.isRevealed(cur)) {
            return;
        }
        session.setRevealed(cur, true);
        go(cur);
    }

    private void refreshStatus() {
        int answered = session.countAnswered();
        progress.setValue(answered);
        progress.setString(answered + " / " + session.size() + " risposte");
        Question q = session.getQuestion(session.getCurrent());
        String info = q.getType().getLabel();
        if (session.getMode() == ExamMode.STUDY) {
            info = q.getDomain().getLabel() + "  ·  " + info;
        }
        infoLabel.setText(info);
    }

    private void openItemReview() {
        ItemReviewDialog d = new ItemReviewDialog(javax.swing.SwingUtilities.getWindowAncestor(this), session);
        d.setVisible(true);
        if (d.isEndRequested()) {
            confirmEnd();
        } else if (d.getSelectedIndex() >= 0) {
            go(d.getSelectedIndex());
        }
    }

    // ------------------------------------------------------------------ tempo e fine

    private void tick() {
        session.tick();
        updateTimer();
        if (session.isTimed() && session.getRemainingSeconds() == 0) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "Tempo scaduto! L'esame viene consegnato.",
                "Tempo scaduto", JOptionPane.WARNING_MESSAGE);
            finish();
        }
    }

    private void updateTimer() {
        if (session.isTimed()) {
            int rem = session.getRemainingSeconds();
            timerLabel.setText("⏱ " + Text.formatDuration(rem));
            timerLabel.setForeground(rem <= 300 ? new Color(0xFFB4A9) : Ui.HEADER_FG);
        } else {
            timerLabel.setText("⏱ " + Text.formatDuration(session.getElapsedSeconds()));
        }
    }

    private void confirmEnd() {
        int unanswered = session.size() - session.countAnswered();
        String msg = "Vuoi terminare e consegnare l'esame?";
        if (unanswered > 0) {
            msg = "Ci sono ancora " + unanswered + " domande senza risposta completa.\n" + msg;
        }
        if (session.countMarked() > 0) {
            msg = "Hai " + session.countMarked() + " domande marcate per revisione.\n" + msg;
        }
        int c = JOptionPane.showConfirmDialog(this, msg, "Termina esame", JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (c == JOptionPane.YES_OPTION) {
            finish();
        }
    }

    private void finish() {
        if (finished) {
            return;
        }
        finished = true;
        timer.stop();
        onFinish.accept(session);
    }

    /** Ferma il timer senza consegnare (es. chiusura finestra). */
    public void abort() {
        finished = true;
        timer.stop();
    }
}
