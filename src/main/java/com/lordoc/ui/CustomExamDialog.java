package com.lordoc.ui;

import com.lordoc.engine.ExamConfig;
import com.lordoc.engine.ExamFactory;
import com.lordoc.engine.QuestionFilter;
import com.lordoc.model.Domain;
import com.lordoc.model.ExamMode;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Finestra di configurazione di un esame personalizzato. */
public final class CustomExamDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final Map<Domain, JCheckBox> domainBoxes = new EnumMap<>(Domain.class);
    private final JTextField title = new JTextField("Esame personalizzato", 24);
    private final JComboBox<ExamMode> mode = new JComboBox<>(ExamMode.values());
    private final JComboBox<QuestionFilter> filter = new JComboBox<>(QuestionFilter.values());
    private final JSpinner count = new JSpinner(new SpinnerNumberModel(30, 1, 500, 5));
    private final JSpinner subnet = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    private final JSpinner minutes = new JSpinner(new SpinnerNumberModel(36, 0, 600, 5));
    private final JCheckBox weighted = new JCheckBox("Distribuisci secondo i pesi del blueprint", true);
    private final JCheckBox allowBack = new JCheckBox("Consenti di tornare indietro", true);
    private ExamConfig result;

    public CustomExamDialog(Window owner) {
        super(owner, "Esame personalizzato", ModalityType.APPLICATION_MODAL);

        JPanel domains = new JPanel(new GridLayout(0, 1));
        domains.setBorder(Ui.titled("Domini"));
        for (Domain d : Domain.values()) {
            JCheckBox cb = new JCheckBox(d.getLabel() + " (" + d.getWeight() + "%)", true);
            domainBoxes.put(d, cb);
            domains.add(cb);
        }
        JPanel selButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        selButtons.add(Ui.button("Tutti", () -> domainBoxes.values().forEach(b -> b.setSelected(true))));
        selButtons.add(Ui.button("Nessuno", () -> domainBoxes.values().forEach(b -> b.setSelected(false))));
        domains.add(selButtons);

        JPanel opts = new JPanel(new GridBagLayout());
        opts.setBorder(Ui.titled("Opzioni"));
        int y = 0;
        y = addField(opts, y, "Titolo:", title);
        y = addField(opts, y, "Modalità:", mode);
        y = addField(opts, y, "Filtro domande:", filter);
        y = addField(opts, y, "Numero di domande:", count);
        y = addField(opts, y, "Domande di subnetting generate:", subnet);
        y = addField(opts, y, "Tempo (minuti, 0 = illimitato):", minutes);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y++;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        opts.add(weighted, c);
        c.gridy = y;
        opts.add(allowBack, c);

        count.addChangeListener(e -> updateMinutes());
        subnet.addChangeListener(e -> updateMinutes());
        mode.addActionListener(e -> updateMinutes());
        mode.setSelectedItem(ExamMode.STUDY);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(Ui.padding(10, 10));
        center.add(domains, BorderLayout.WEST);
        center.add(opts, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(Ui.button("Annulla", this::dispose));
        buttons.add(Ui.button("Crea esame", this::confirm));

        JPanel root = new JPanel(new BorderLayout());
        root.add(Ui.header("Esame personalizzato", "Allena i punti deboli: scegli domini e filtri"),
            BorderLayout.NORTH);
        root.add(center, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    private static int addField(JPanel p, int y, String label, java.awt.Component field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 4, 4, 8);
        p.add(new JLabel(label), c);
        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        p.add(field, c);
        return y + 1;
    }

    private void updateMinutes() {
        if (mode.getSelectedItem() == ExamMode.STUDY) {
            minutes.setValue(0);
        } else {
            int n = (Integer) count.getValue() + (Integer) subnet.getValue();
            minutes.setValue(ExamFactory.suggestedMinutes(n));
        }
    }

    private void confirm() {
        Set<Domain> sel = EnumSet.noneOf(Domain.class);
        domainBoxes.forEach((d, b) -> {
            if (b.isSelected()) {
                sel.add(d);
            }
        });
        if (sel.isEmpty() && (Integer) subnet.getValue() == 0) {
            JOptionPane.showMessageDialog(this, "Seleziona almeno un dominio o delle domande di subnetting.");
            return;
        }
        result = new ExamConfig()
            .title(title.getText().isBlank() ? "Esame personalizzato" : title.getText().trim())
            .mode((ExamMode) mode.getSelectedItem())
            .domains(sel)
            .filter((QuestionFilter) filter.getSelectedItem())
            .questionCount((Integer) count.getValue())
            .subnetQuestions((Integer) subnet.getValue())
            .timeMinutes((Integer) minutes.getValue())
            .weighted(weighted.isSelected())
            .allowBack(allowBack.isSelected());
        dispose();
    }

    /** null se l'utente ha annullato. */
    public ExamConfig getResult() { return result; }
}
