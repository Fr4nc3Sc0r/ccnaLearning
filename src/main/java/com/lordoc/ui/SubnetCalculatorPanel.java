package com.lordoc.ui;

import com.lordoc.util.IpUtils;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

/** Calcolatore IPv4: rete, broadcast, range host, maschera, wildcard, binario. */
public final class SubnetCalculatorPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JTextField input = new JTextField("192.168.10.77/26", 22);
    private final DefaultTableModel model = new DefaultTableModel(new Object[] {"Campo", "Valore"}, 0) {
        private static final long serialVersionUID = 1L;
        @Override
        public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JLabel error = Ui.label(" ", Ui.BOLD, Ui.KO);

    public SubnetCalculatorPanel() {
        super(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        top.add(Ui.label("Indirizzo (es. 10.1.2.3/20 oppure 10.1.2.3 255.255.240.0):", Ui.BOLD, null));
        input.setFont(Ui.MONO.deriveFont(16f));
        input.addActionListener(e -> calculate());
        top.add(input);
        top.add(Ui.button("Calcola", this::calculate));
        top.add(error);

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(Ui.MONO.deriveFont(15f));
        table.getColumnModel().getColumn(0).setMinWidth(230);
        table.getColumnModel().getColumn(0).setMaxWidth(260);

        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(Ui.padding(4, 8));
        center.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(new WrapLabel("Suggerimento: all'esame non hai calcolatrici. Usa questo strumento solo per "
            + "verificare i calcoli fatti a mano con il metodo del magic number (256 - valore della maschera "
            + "nell'ottetto interessante).", Ui.SMALL), BorderLayout.SOUTH);

        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        calculate();
    }

    private void calculate() {
        model.setRowCount(0);
        error.setText(" ");
        try {
            String s = input.getText().trim();
            String ipPart;
            int prefix;
            if (s.contains("/")) {
                String[] p = s.split("/");
                ipPart = p[0];
                prefix = Integer.parseInt(p[1].trim());
            } else {
                String[] p = s.split("\\s+");
                if (p.length != 2) {
                    throw new IllegalArgumentException("Specifica prefisso (/24) o maschera");
                }
                ipPart = p[0];
                prefix = IpUtils.prefixFromMask(p[1]);
            }
            long ip = IpUtils.toLong(ipPart);
            long net = IpUtils.network(ip, prefix);
            long bc = IpUtils.broadcast(ip, prefix);
            add("Indirizzo", IpUtils.toIp(ip));
            add("Prefisso / maschera", "/" + prefix + "  =  " + IpUtils.maskString(prefix));
            add("Wildcard mask", IpUtils.wildcardString(prefix));
            add("Indirizzo di rete", IpUtils.toIp(net));
            add("Broadcast", IpUtils.toIp(bc));
            if (prefix <= 30) {
                add("Primo host", IpUtils.toIp(net + 1));
                add("Ultimo host", IpUtils.toIp(bc - 1));
            } else if (prefix == 31) {
                add("Host (RFC 3021)", IpUtils.toIp(net) + " – " + IpUtils.toIp(bc));
            }
            add("Host utilizzabili", String.valueOf(IpUtils.usableHosts(prefix)));
            add("Indirizzi totali", String.valueOf(1L << (32 - prefix)));
            if (prefix > 0 && prefix % 8 != 0) {
                int oct = (prefix - 1) / 8;
                int maskOct = (int) ((IpUtils.mask(prefix) >> (24 - 8 * oct)) & 255);
                add("Magic number", (256 - maskOct) + " nel " + (oct + 1) + "° ottetto");
            }
            add("Classe (classful)", String.valueOf(IpUtils.ipClass(ip)));
            add("Privato RFC 1918", IpUtils.isPrivate(ip) ? "sì" : "no");
            add("Binario indirizzo", IpUtils.toBinary(ip));
            add("Binario maschera", IpUtils.toBinary(IpUtils.mask(prefix)));
        } catch (RuntimeException ex) {
            error.setText(ex.getMessage() == null ? "Input non valido" : ex.getMessage());
        }
    }

    private void add(String k, String v) { model.addRow(new Object[] {k, v}); }
}
