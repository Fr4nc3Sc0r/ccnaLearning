package com.lordoc.ui;

import com.lordoc.model.Question;
import com.lordoc.model.QuestionType;
import com.lordoc.model.Response;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Visualizza una domanda con l'area di risposta adatta al tipo (radio, checkbox,
 * abbinamento, campo di testo). In modalità "reveal" mostra correzione e spiegazione.
 */
public final class QuestionView extends JPanel {
    private static final long serialVersionUID = 1L;

    private final ScrollablePanel content = new ScrollablePanel(new GridBagLayout());
    private final JScrollPane scroll;
    private Runnable onChange = () -> { };

    public QuestionView() {
        super(new BorderLayout());
        content.setBackground(Ui.PANEL_BG);
        content.setBorder(Ui.padding(16, 22));
        scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Ui.PANEL_BG);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    public void setOnChange(Runnable r) { this.onChange = r == null ? () -> { } : r; }

    /**
     * @param editable se l'utente può modificare la risposta
     * @param reveal   se mostrare la correzione e la spiegazione
     */
    public void display(Question q, Response r, boolean editable, boolean reveal) {
        content.removeAll();
        int y = 0;

        WrapLabel text = new WrapLabel(q.getDisplayText(), Ui.BASE.deriveFont(16f));
        content.add(text, Ui.row(y++));

        if (q.hasExhibit()) {
            content.add(exhibit(q.getExhibit()), Ui.row(y++));
        }

        JPanel answers = new JPanel(new GridBagLayout());
        answers.setOpaque(false);
        switch (q.getType()) {
            case SINGLE:
            case MULTI:
                buildChoices(answers, q, r, editable, reveal);
                break;
            case DRAG:
                buildDrag(answers, q, r, editable, reveal);
                break;
            case FILL:
                buildFill(answers, q, r, editable, reveal);
                break;
            default:
                break;
        }
        GridBagConstraints ac = Ui.row(y++);
        ac.insets = new Insets(12, 0, 4, 0);
        content.add(answers, ac);

        if (reveal) {
            content.add(feedback(q, r), Ui.row(y++));
        }
        Ui.addFiller(content, y);
        content.revalidate();
        content.repaint();
        SwingUtilities.invokeLater(() -> scroll.getVerticalScrollBar().setValue(0));
    }

    // ------------------------------------------------------------------ exhibit

    private JPanel exhibit(String exhibitText) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = Ui.label("Exhibit", Ui.BOLD.deriveFont(13f), Ui.ACCENT);
        title.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0));
        p.add(title, BorderLayout.NORTH);
        JTextArea area = new JTextArea(exhibitText);
        area.setFont(Ui.MONO);
        area.setEditable(false);
        area.setBackground(Ui.EXHIBIT_BG);
        area.setForeground(Ui.EXHIBIT_FG);
        area.setCaretColor(Ui.EXHIBIT_FG);
        area.setBorder(Ui.padding(10, 12));
        JScrollPane sp = new JScrollPane(area, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0x444444)));
        int h = area.getPreferredSize().height + sp.getHorizontalScrollBar().getPreferredSize().height + 4;
        sp.setPreferredSize(new Dimension(100, h));
        sp.setMinimumSize(new Dimension(100, h));
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    // ------------------------------------------------------------------ choices

    private void buildChoices(JPanel panel, Question q, Response r, boolean editable, boolean reveal) {
        boolean single = q.getType() == QuestionType.SINGLE;
        ButtonGroup group = new ButtonGroup();
        List<String> opts = q.getOptions();
        for (int i = 0; i < opts.size(); i++) {
            final int idx = i;
            AbstractButton btn = single ? new JRadioButton() : new JCheckBox();
            btn.setText(Question.letter(i) + ".");
            btn.setFont(Ui.BOLD);
            btn.setOpaque(false);
            btn.setSelected(r.getSelected().contains(i));
            btn.setEnabled(editable);
            btn.setVerticalAlignment(AbstractButton.TOP);
            if (single) {
                group.add(btn);
            }
            btn.addItemListener(e -> {
                if (btn.isSelected()) {
                    r.select(idx, single);
                } else {
                    r.deselect(idx);
                }
                onChange.run();
            });

            WrapLabel lbl = new WrapLabel(opts.get(i), Ui.BASE);
            lbl.setBorder(BorderFactory.createEmptyBorder(3, 4, 0, 0));
            if (editable) {
                lbl.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (single) {
                            btn.setSelected(true);
                        } else {
                            btn.setSelected(!btn.isSelected());
                        }
                    }
                });
            }

            JPanel row = new JPanel(new BorderLayout());
            row.setBorder(Ui.padding(4, 6));
            row.add(btn, BorderLayout.WEST);
            row.add(lbl, BorderLayout.CENTER);
            row.setOpaque(false);
            if (reveal) {
                boolean correct = q.getCorrectOptions().contains(i);
                boolean chosen = r.getSelected().contains(i);
                if (correct) {
                    row.setOpaque(true);
                    row.setBackground(Ui.OK_BG);
                    row.add(Ui.label(chosen ? "✔ corretta (scelta)" : "✔ corretta", Ui.BOLD, Ui.OK),
                        BorderLayout.EAST);
                } else if (chosen) {
                    row.setOpaque(true);
                    row.setBackground(Ui.KO_BG);
                    row.add(Ui.label("✘ scelta errata", Ui.BOLD, Ui.KO), BorderLayout.EAST);
                }
            }
            panel.add(row, Ui.row(i));
        }
        if (!single && editable) {
            JLabel hint = Ui.label("Seleziona " + q.requiredSelections() + " risposte.", Ui.SMALL, Color.GRAY);
            panel.add(hint, Ui.row(opts.size()));
        }
    }

    // ------------------------------------------------------------------ drag & drop (abbinamento)

    private void buildDrag(JPanel panel, Question q, Response r, boolean editable, boolean reveal) {
        JLabel hint = Ui.label("Abbina ogni elemento a sinistra alla categoria corretta.", Ui.SMALL, Color.GRAY);
        panel.add(hint, Ui.row(0));
        List<String> targets = new ArrayList<>();
        targets.add("");
        targets.addAll(q.getDragTargets());
        List<String> items = q.getDragItems();
        for (int i = 0; i < items.size(); i++) {
            final int idx = i;
            JPanel row = new JPanel(new GridBagLayout());
            row.setBorder(Ui.padding(4, 6));
            row.setOpaque(false);

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 0, 0, 10);
            WrapLabel item = new WrapLabel(items.get(i), Ui.BOLD);
            row.add(item, c);

            JComboBox<String> combo = new JComboBox<>(targets.toArray(new String[0]));
            combo.setFont(Ui.BASE);
            String cur = r.getDragAnswer(i);
            combo.setSelectedItem(cur == null ? "" : cur);
            combo.setEnabled(editable);
            combo.setPrototypeDisplayValue(longest(targets));
            combo.addActionListener(e -> {
                r.setDragAnswer(idx, (String) combo.getSelectedItem());
                onChange.run();
            });
            c.gridx = 1;
            c.weightx = 0;
            c.fill = GridBagConstraints.NONE;
            row.add(combo, c);

            if (reveal) {
                String expected = q.getDragSolution().get(i);
                boolean ok = expected.equals(cur);
                row.setOpaque(true);
                row.setBackground(ok ? Ui.OK_BG : Ui.KO_BG);
                c.gridx = 2;
                row.add(Ui.label(ok ? "✔" : "✘ → " + expected, Ui.BOLD, ok ? Ui.OK : Ui.KO), c);
            }
            panel.add(row, Ui.row(i + 1));
        }
    }

    private static String longest(List<String> l) {
        String best = "XXXXXXXXXX";
        for (String s : l) {
            if (s.length() > best.length()) {
                best = s;
            }
        }
        return best;
    }

    // ------------------------------------------------------------------ fill

    private void buildFill(JPanel panel, Question q, Response r, boolean editable, boolean reveal) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.add(Ui.label("Risposta:", Ui.BOLD, null));
        JTextField field = new JTextField(r.getFillText(), 24);
        field.setFont(Ui.MONO.deriveFont(16f));
        field.setEditable(editable);
        field.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                r.setFillText(field.getText());
                onChange.run();
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });
        row.add(field);
        if (reveal) {
            boolean ok = q.isCorrect(r);
            field.setBackground(ok ? Ui.OK_BG : Ui.KO_BG);
        }
        panel.add(row, Ui.row(0));
        if (editable) {
            SwingUtilities.invokeLater(field::requestFocusInWindow);
        }
    }

    // ------------------------------------------------------------------ feedback

    private JPanel feedback(Question q, Response r) {
        boolean ok = q.isCorrect(r);
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(0xF6F8FB));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, ok ? Ui.OK : Ui.KO), Ui.padding(10, 14)));
        int y = 0;
        String verdict;
        if (ok) {
            verdict = "✔ Risposta corretta";
        } else if (!q.isAnswered(r)) {
            verdict = "✘ Risposta mancante o incompleta";
        } else {
            verdict = "✘ Risposta errata";
        }
        p.add(Ui.label(verdict, Ui.BOLD.deriveFont(17f), ok ? Ui.OK : Ui.KO), Ui.row(y++));
        p.add(new WrapLabel("Risposta corretta: " + q.getCorrectAnswerText(), Ui.BOLD), Ui.row(y++));
        if (!q.getExplanation().isBlank()) {
            JLabel h = Ui.label("Spiegazione", Ui.BOLD, Ui.ACCENT);
            h.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
            p.add(h, Ui.row(y++));
            p.add(new WrapLabel(plain(q.getExplanation()), Ui.BASE), Ui.row(y++));
        }
        if (!q.getReference().isBlank()) {
            p.add(new WrapLabel("Riferimento: " + q.getReference(), Ui.SMALL.deriveFont(java.awt.Font.ITALIC)),
                Ui.row(y++));
        }
        p.add(Ui.label("ID domanda: " + q.getId() + "  ·  " + q.getDomain().getLabel() + "  ·  "
            + q.getType().getLabel(), Ui.SMALL, Color.GRAY), Ui.row(y));
        return p;
    }

    /** Rimuove la mini-sintassi `codice` e **grassetto** per la visualizzazione in testo semplice. */
    static String plain(String s) {
        return s.replace("`", "").replace("**", "");
    }
}
