package com.ccnaexamlab.ui;

import com.ccnaexamlab.app.AppContext;
import com.ccnaexamlab.engine.Scorer;
import com.ccnaexamlab.model.ExamMode;
import com.ccnaexamlab.model.ExamResult;
import com.ccnaexamlab.model.ExamSession;
import com.ccnaexamlab.model.Question;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/** Finestra principale: alterna menu, esame in corso e risultati. */
public final class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final AppContext ctx;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final HomePanel home;
    private ExamPanel examPanel;
    private JPanel resultsPanel;

    public MainFrame(AppContext ctx) {
        super(AppContext.APP_NAME + " — Simulatore d'esame CCNA 200-301");
        this.ctx = ctx;
        this.home = new HomePanel(ctx, this::startExam);
        root.add(home, "home");
        setContentPane(root);
        setJMenuBar(buildMenu());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { exit(); }
        });
        setMinimumSize(new Dimension(980, 680));
        setSize(1240, 840);
        setLocationRelativeTo(null);
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem home = new JMenuItem("Menu principale");
        home.addActionListener(e -> {
            if (examPanel == null || confirmAbort()) {
                showHome();
            }
        });
        JMenuItem dataDir = new JMenuItem("Cartella dati utente...");
        dataDir.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "Storico e statistiche sono salvati in:\n" + ctx.getStore().getDirectory()
                + "\n\nPuoi aggiungere le tue domande (file .txt) in:\n"
                + ctx.getStore().getUserQuestionsDirectory()
                + "\n\nIl formato è descritto nel README del progetto.",
            "Cartella dati", JOptionPane.INFORMATION_MESSAGE));
        JMenuItem exit = new JMenuItem("Esci");
        exit.addActionListener(e -> exit());
        file.add(home);
        file.add(dataDir);
        file.addSeparator();
        file.add(exit);

        JMenu help = new JMenu("Aiuto");
        JMenuItem guide = new JMenuItem("Guida rapida");
        guide.addActionListener(e -> JOptionPane.showMessageDialog(this, HELP, "Guida rapida",
            JOptionPane.INFORMATION_MESSAGE));
        JMenuItem about = new JMenuItem("Informazioni");
        about.addActionListener(e -> JOptionPane.showMessageDialog(this,
            AppContext.APP_NAME + " " + AppContext.VERSION + "\n"
                + "Simulatore d'esame open source per la preparazione al Cisco CCNA 200-301.\n"
                + "Domande nella banca: " + ctx.getBank().size() + "\n\n"
                + "Progetto indipendente, non affiliato a Cisco Systems.\n"
                + "CCNA è un marchio registrato di Cisco Systems, Inc.",
            "Informazioni", JOptionPane.INFORMATION_MESSAGE));
        help.add(guide);
        help.add(about);
        bar.add(file);
        bar.add(help);
        return bar;
    }

    private static final String HELP =
        "MODALITÀ\n"
        + "• Simulazione: tempo limite (~1,2 min/domanda), nessuna spiegazione fino alla consegna.\n"
        + "• Studio: senza limite di tempo; 'Mostra risposta' (Ctrl+R) rivela correzione e spiegazione.\n\n"
        + "DURANTE L'ESAME\n"
        + "• Alt+→ / Alt+← : domanda successiva / precedente\n"
        + "• Ctrl+M : marca la domanda per rivederla\n"
        + "• 'Riepilogo domande' mostra risposte incomplete e marcate.\n"
        + "• Se disattivi 'consenti di tornare indietro' l'esame si comporta come quello reale Cisco.\n\n"
        + "DOPO L'ESAME\n"
        + "• Il report mostra il punteggio (scala 300-1000) e il dettaglio per dominio.\n"
        + "• Doppio clic su una domanda per rivederla con la spiegazione.\n"
        + "• 'Ripassa errori' crea subito un esame Studio con le sole domande sbagliate.\n\n"
        + "METODO CONSIGLIATO\n"
        + "1) Studio per argomento  2) Simulazione completa  3) Ripasso degli errori\n"
        + "4) Ripeti finché non superi stabilmente 900 in simulazione.";

    // ------------------------------------------------------------------ flussi

    public void startExam(ExamSession session) {
        if (session == null) {
            return;
        }
        examPanel = new ExamPanel(session, this::finishExam);
        root.add(examPanel, "exam");
        cards.show(root, "exam");
    }

    private void finishExam(ExamSession session) {
        ExamResult result = Scorer.score(session);
        ctx.getStore().recordResult(result);
        if (examPanel != null) {
            root.remove(examPanel);
            examPanel = null;
        }
        if (resultsPanel != null) {
            root.remove(resultsPanel);
        }
        resultsPanel = new ResultsPanel(result, this::showHome, this::retryWrong);
        root.add(resultsPanel, "results");
        cards.show(root, "results");
        home.refresh();
    }

    private void retryWrong(List<Question> wrong) {
        startExam(ctx.getFactory().fromQuestions("Ripasso errori", ExamMode.STUDY, wrong));
    }

    private void showHome() {
        if (examPanel != null) {
            examPanel.abort();
            root.remove(examPanel);
            examPanel = null;
        }
        home.refresh();
        cards.show(root, "home");
    }

    private boolean confirmAbort() {
        int c = JOptionPane.showConfirmDialog(this,
            "C'è un esame in corso: abbandonandolo le risposte andranno perse. Continuare?",
            "Esame in corso", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return c == JOptionPane.YES_OPTION;
    }

    private void exit() {
        if (examPanel != null && !confirmAbort()) {
            return;
        }
        dispose();
        System.exit(0);
    }
}
