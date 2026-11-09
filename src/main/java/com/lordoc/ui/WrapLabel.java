package com.lordoc.ui;

import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.Font;

/** Etichetta multilinea con a-capo automatico (basata su JTextArea non modificabile). */
public class WrapLabel extends JTextArea {
    private static final long serialVersionUID = 1L;

    public WrapLabel(String text, Font font) {
        super(text);
        setFont(font);
        setLineWrap(true);
        setWrapStyleWord(true);
        setEditable(false);
        setFocusable(false);
        setOpaque(false);
        setBorder(null);
        setHighlighter(null);
        setForeground(Color.BLACK);
    }
}
