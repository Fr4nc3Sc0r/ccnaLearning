package com.ccnaexamlab.util;

/** Piccole utilità per testo e formattazione. */
public final class Text {

    private Text() { }

    public static String escapeHtml(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '&': sb.append("&amp;"); break;
                case '"': sb.append("&quot;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }

    /** Converte testo semplice in HTML, rendendo `codice` in monospace e le righe vuote in paragrafi. */
    public static String toHtml(String s) {
        String e = escapeHtml(s);
        e = e.replaceAll("`([^`]+)`", "<code>$1</code>");
        e = e.replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>");
        return e.replace("\n\n", "<br><br>").replace("\n", "<br>");
    }

    public static String formatDuration(int seconds) {
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }

    /** Elimina righe vuote iniziali e spazi finali, preservando l'indentazione interna. */
    public static String trimBlock(String s) {
        if (s == null) {
            return "";
        }
        String r = s.stripTrailing();
        while (r.startsWith("\n") || r.startsWith("\r")) {
            r = r.substring(1);
        }
        return r;
    }
}
