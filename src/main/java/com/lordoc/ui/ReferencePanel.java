package com.lordoc.ui;

import javax.swing.DefaultListModel;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Scheda "Cheat sheet": schede di ripasso in HTML caricate dalle risorse. */
public final class ReferencePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final List<String> files = new ArrayList<>();
    private final JEditorPane pane = new JEditorPane();

    public ReferencePanel() {
        super(new BorderLayout());
        DefaultListModel<String> titles = new DefaultListModel<>();
        loadIndex(titles);

        JList<String> list = new JList<>(titles);
        list.setFont(Ui.BASE);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(30);
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && list.getSelectedIndex() >= 0) {
                show(files.get(list.getSelectedIndex()));
            }
        });

        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = kit.getStyleSheet();
        css.addRule("body { font-family: sans-serif; font-size: 13pt; margin: 12px; color: #1b1b1b; }");
        css.addRule("h1 { color: #1F3A5F; font-size: 20pt; }");
        css.addRule("h2 { color: #2E6DB4; font-size: 15pt; margin-top: 14px; }");
        css.addRule("th { background-color: #1F3A5F; color: white; padding: 4px; text-align: left; }");
        css.addRule("td { padding: 4px; border-bottom: 1px solid #dddddd; }");
        css.addRule("code, pre { font-family: monospace; font-size: 12pt; background-color: #f1f3f6; }");
        css.addRule(".tip { background-color: #fff6dc; padding: 6px; border-left: 4px solid #e69500; }");
        pane.setEditorKit(kit);
        pane.setEditable(false);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), new JScrollPane(pane));
        split.setDividerLocation(260);
        split.setBorder(Ui.padding(8, 8));
        add(split, BorderLayout.CENTER);
        if (!files.isEmpty()) {
            list.setSelectedIndex(0);
        }
    }

    private void loadIndex(DefaultListModel<String> titles) {
        InputStream in = getClass().getResourceAsStream("/reference/index.txt");
        if (in == null) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                int sep = line.indexOf('|');
                if (line.isBlank() || line.startsWith("#") || sep < 0) {
                    continue;
                }
                files.add(line.substring(0, sep).trim());
                titles.addElement(line.substring(sep + 1).trim());
            }
        } catch (IOException e) {
            titles.addElement("Errore: " + e.getMessage());
        }
    }

    private void show(String file) {
        InputStream in = getClass().getResourceAsStream("/reference/" + file);
        if (in == null) {
            pane.setText("<html><body>Risorsa non trovata: " + file + "</body></html>");
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            pane.setText(br.lines().collect(Collectors.joining("\n")));
        } catch (IOException e) {
            pane.setText("<html><body>Errore: " + e.getMessage() + "</body></html>");
        }
        SwingUtilities.invokeLater(() -> pane.setCaretPosition(0));
    }
}
