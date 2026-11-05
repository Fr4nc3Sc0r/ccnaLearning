package com.lordoc.app;

import com.lordoc.data.QuestionBank;
import com.lordoc.data.UserDataStore;
import com.lordoc.ui.MainFrame;

import java.nio.file.Path;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Punto di ingresso dell'applicazione. */
public final class Main {

    private Main() { }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        if (args.length > 0 && args[0].equals("--validate")) {
            System.exit(validate());
        }

        SwingUtilities.invokeLater(() -> {
            setLookAndFeel();
            Path dataDir = UserDataStore.defaultDirectory();
            UserDataStore.migrateLegacyDirectory(dataDir);
            UserDataStore store = new UserDataStore(dataDir);
            store.load();
            QuestionBank bank = QuestionBank.load(store.getUserQuestionsDirectory());
            AppContext ctx = new AppContext(bank, store);
            MainFrame frame = new MainFrame(ctx);
            frame.setVisible(true);
            if (!bank.getErrors().isEmpty()) {
                StringBuilder sb = new StringBuilder("Alcune domande non sono state caricate:\n\n");
                bank.getErrors().stream().limit(15).forEach(e -> sb.append("• ").append(e).append('\n'));
                JOptionPane.showMessageDialog(frame, sb.toString(), "Avvisi caricamento domande",
                    JOptionPane.WARNING_MESSAGE);
            }
        });
    }

    /** Modalità a riga di comando: verifica i file delle domande (utile prima di un commit). */
    private static int validate() {
        QuestionBank bank = QuestionBank.load(null);
        System.out.println("Domande caricate: " + bank.size());
        bank.countByDomain().forEach((d, n) -> System.out.printf("  %-40s %3d%n", d.getLabel(), n));
        bank.getErrors().forEach(e -> System.out.println("ERRORE: " + e));
        return bank.getErrors().isEmpty() ? 0 : 1;
    }

    private static void setLookAndFeel() {
        String laf = System.getProperty("lordoc.laf");
        try {
            if (laf != null) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().equalsIgnoreCase(laf)) {
                        UIManager.setLookAndFeel(info.getClassName());
                        return;
                    }
                }
            }
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
            System.err.println("Look and feel non disponibile: " + e.getMessage());
        }
    }
}
