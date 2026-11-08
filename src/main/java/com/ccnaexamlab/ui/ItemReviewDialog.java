package com.ccnaexamlab.ui;

import com.ccnaexamlab.model.ExamMode;
import com.ccnaexamlab.model.ExamSession;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Riepilogo di tutte le domande (risposte/incomplete/marcate) con salto rapido. */
public final class ItemReviewDialog extends JDialog {
    private static final long serialVersionUID = 1L;

    private final ExamSession session;
    private final JTable table;
    private int selectedIndex = -1;
    private boolean endRequested;

    public ItemReviewDialog(Window owner, ExamSession session) {
        super(owner, "Riepilogo domande", ModalityType.APPLICATION_MODAL);
        this.session = session;
        boolean study = session.getMode() == ExamMode.STUDY;

        table = new JTable(new AbstractTableModel() {
            private static final long serialVersionUID = 1L;
            private final String[] cols = study
                ? new String[] {"#", "Stato", "Marcata", "Dominio"}
                : new String[] {"#", "Stato", "Marcata"};

            @Override public int getRowCount() { return session.size(); }
            @Override public int getColumnCount() { return cols.length; }
            @Override public String getColumnName(int c) { return cols[c]; }

            @Override
            public Object getValueAt(int r, int c) {
                switch (c) {
                    case 0: return r + 1;
                    case 1: return session.isAnswered(r) ? "Risposta data" : "Incompleta";
                    case 2: return session.isMarked(r) ? "⚑ Marcata" : "";
                    default: return session.getQuestion(r).getDomain().getLabel();
                }
            }
        });
        table.setRowHeight(24);
        table.setFont(Ui.BASE.deriveFont(14f));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc,
                                                           int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    c.setForeground(col == 1 && !session.isAnswered(row) ? Ui.KO
                        : col == 2 ? Ui.WARN : Color.BLACK);
                }
                return c;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    jump(table.getSelectedRow());
                }
            }
        });
        if (session.getCurrent() < session.size()) {
            table.setRowSelectionInterval(session.getCurrent(), session.getCurrent());
        }

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(Ui.button("Vai alla selezionata", () -> {
            if (table.getSelectedRow() >= 0) {
                jump(table.getSelectedRow());
            }
        }));
        buttons.add(Ui.button("Prima incompleta", () -> jumpFirst(false)));
        buttons.add(Ui.button("Prima marcata", () -> jumpFirst(true)));
        JButton end = Ui.button("Termina esame", () -> {
            endRequested = true;
            dispose();
        });
        end.setForeground(Ui.KO);
        buttons.add(end);
        buttons.add(Ui.button("Chiudi", this::dispose));

        JPanel root = new JPanel(new BorderLayout());
        root.add(Ui.header("Riepilogo", session.countAnswered() + " risposte su " + session.size()
            + "  ·  " + session.countMarked() + " marcate"), BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        setSize(720, 560);
        setLocationRelativeTo(owner);
    }

    private void jump(int idx) {
        selectedIndex = idx;
        dispose();
    }

    private void jumpFirst(boolean marked) {
        for (int i = 0; i < session.size(); i++) {
            if (marked ? session.isMarked(i) : !session.isAnswered(i)) {
                jump(i);
                return;
            }
        }
        javax.swing.JOptionPane.showMessageDialog(this,
            marked ? "Nessuna domanda marcata." : "Hai risposto a tutte le domande.");
    }

    public int getSelectedIndex() { return selectedIndex; }

    public boolean isEndRequested() { return endRequested; }
}
