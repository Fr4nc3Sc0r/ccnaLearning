package com.lordoc.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

/** Costanti grafiche e piccoli helper Swing condivisi. */
public final class Ui {

    public static final Color HEADER_BG = new Color(0x1F3A5F);
    public static final Color HEADER_FG = Color.WHITE;
    public static final Color SUBHEADER_BG = new Color(0xE8EEF5);
    public static final Color ACCENT = new Color(0x2E6DB4);
    public static final Color OK = new Color(0x2E7D32);
    public static final Color OK_BG = new Color(0xE3F4E4);
    public static final Color KO = new Color(0xC62828);
    public static final Color KO_BG = new Color(0xFBE4E4);
    public static final Color WARN = new Color(0xE69500);
    public static final Color EXHIBIT_BG = new Color(0x1E1E1E);
    public static final Color EXHIBIT_FG = new Color(0xE6E6E6);
    public static final Color PANEL_BG = Color.WHITE;

    public static final Font BASE = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    public static final Font BOLD = BASE.deriveFont(Font.BOLD);
    public static final Font SMALL = BASE.deriveFont(13f);
    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 22);
    public static final Font HUGE = new Font(Font.SANS_SERIF, Font.BOLD, 44);
    public static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 14);

    private Ui() { }

    public static JButton button(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setFont(BOLD.deriveFont(14f));
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }

    public static JLabel label(String text, Font font, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        if (fg != null) {
            l.setForeground(fg);
        }
        return l;
    }

    public static Border padding(int v, int h) {
        return BorderFactory.createEmptyBorder(v, h, v, h);
    }

    public static Border titled(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0xC5CED9)), title),
            padding(6, 8));
    }

    /** Pannello "header" scuro con titolo. */
    public static JPanel header(String title, String subtitle) {
        JPanel p = new JPanel(new java.awt.BorderLayout());
        p.setBackground(HEADER_BG);
        p.setBorder(padding(12, 18));
        JLabel t = label(title, TITLE, HEADER_FG);
        p.add(t, java.awt.BorderLayout.NORTH);
        if (subtitle != null) {
            p.add(label(subtitle, SMALL, new Color(0xC9D6E6)), java.awt.BorderLayout.SOUTH);
        }
        return p;
    }

    /** Vincoli GridBag per impilare componenti a tutta larghezza. */
    public static GridBagConstraints row(int y) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = y;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(4, 0, 4, 0);
        return c;
    }

    /** Aggiunge un riempitivo verticale in fondo a un pannello GridBag. */
    public static void addFiller(JPanel p, int y) {
        GridBagConstraints c = row(y);
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        p.add(filler, c);
    }

    public static void setAllFonts(JComponent root, Font f) {
        for (Component c : root.getComponents()) {
            c.setFont(f);
            if (c instanceof JComponent) {
                setAllFonts((JComponent) c, f);
            }
        }
    }

    public static String pct(double v) {
        return String.format("%.0f%%", v);
    }
}
