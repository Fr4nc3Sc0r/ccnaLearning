package com.lordoc.ui;

import com.lordoc.model.ExamResult;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/** Grafico a linee dell'andamento dei punteggi nel tempo. */
public final class ScoreChart extends JComponent {
    private static final long serialVersionUID = 1L;

    private List<ExamResult> results = new ArrayList<>();

    public ScoreChart() {
        setPreferredSize(new Dimension(400, 220));
    }

    public void setResults(List<ExamResult> list) {
        int from = Math.max(0, list.size() - 30);
        this.results = new ArrayList<>(list.subList(from, list.size()));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        int left = 46;
        int right = 16;
        int top = 16;
        int bottom = 28;
        int w = getWidth() - left - right;
        int h = getHeight() - top - bottom;
        int min = ExamResult.MIN_SCORE;
        int max = ExamResult.MAX_SCORE;

        g.setFont(Ui.SMALL.deriveFont(11f));
        for (int v = min; v <= max; v += 100) {
            int y = top + h - (int) ((double) (v - min) / (max - min) * h);
            g.setColor(new Color(0xEEEEEE));
            g.drawLine(left, y, left + w, y);
            g.setColor(Color.GRAY);
            g.drawString(String.valueOf(v), 8, y + 4);
        }
        int passY = top + h - (int) ((double) (ExamResult.PASSING_SCORE - min) / (max - min) * h);
        g.setColor(Ui.OK);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {5f, 4f}, 0f));
        g.drawLine(left, passY, left + w, passY);
        g.drawString("soglia " + ExamResult.PASSING_SCORE, left + 4, passY - 4);

        if (results.isEmpty()) {
            g.setColor(Color.GRAY);
            g.setFont(Ui.BASE);
            g.drawString("Nessun esame svolto: i tuoi progressi appariranno qui.", left + 20, top + h / 2);
            g.dispose();
            return;
        }
        g.setStroke(new BasicStroke(2.2f));
        int n = results.size();
        int prevX = -1;
        int prevY = -1;
        for (int i = 0; i < n; i++) {
            int x = left + (n == 1 ? w / 2 : (int) ((double) i / (n - 1) * w));
            int s = results.get(i).getScaledScore();
            int y = top + h - (int) ((double) (s - min) / (max - min) * h);
            if (prevX >= 0) {
                g.setColor(Ui.ACCENT);
                g.drawLine(prevX, prevY, x, y);
            }
            g.setColor(s >= ExamResult.PASSING_SCORE ? Ui.OK : Ui.KO);
            g.fillOval(x - 4, y - 4, 8, 8);
            prevX = x;
            prevY = y;
        }
        g.setColor(Color.GRAY);
        g.drawString("Ultimi " + n + " esami", left + w - 90, top + h + 20);
        g.dispose();
    }
}
