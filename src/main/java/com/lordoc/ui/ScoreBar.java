package com.lordoc.ui;

import com.lordoc.model.ExamResult;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Barra orizzontale del punteggio (300-1000) con la soglia di superamento. */
public final class ScoreBar extends JComponent {
    private static final long serialVersionUID = 1L;

    private final int score;

    public ScoreBar(int score) {
        this.score = score;
        setPreferredSize(new Dimension(500, 54));
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth() - 20;
        int x0 = 10;
        int barY = 8;
        int barH = 22;
        int min = ExamResult.MIN_SCORE;
        int max = ExamResult.MAX_SCORE;
        g.setColor(new Color(0xE4E8EE));
        g.fillRoundRect(x0, barY, w, barH, 10, 10);
        int fill = (int) ((double) (score - min) / (max - min) * w);
        g.setColor(score >= ExamResult.PASSING_SCORE ? Ui.OK : Ui.KO);
        g.fillRoundRect(x0, barY, Math.max(fill, 6), barH, 10, 10);
        int passX = x0 + (int) ((double) (ExamResult.PASSING_SCORE - min) / (max - min) * w);
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {4f, 3f}, 0f));
        g.drawLine(passX, barY - 4, passX, barY + barH + 4);
        g.setFont(Ui.SMALL);
        String label = "Soglia " + ExamResult.PASSING_SCORE;
        int lw = g.getFontMetrics().stringWidth(label);
        g.drawString(label, Math.min(passX - lw / 2, x0 + w - lw), barY + barH + 18);
        g.drawString(String.valueOf(min), x0, barY + barH + 18);
        g.dispose();
    }
}
