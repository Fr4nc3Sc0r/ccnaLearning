# Il codice spiegato riga per riga

Questa sezione mostra **tutto il codice sorgente** del programma (40 file, circa 4.200 righe), diviso
in piccoli blocchi. Sotto ogni blocco c'è la spiegazione in italiano di cosa fanno quelle righe.
Ogni riga di ogni file compare esattamente una volta, con il suo numero.

Prima di cominciare conviene leggere [01 - Concetti base](../01-concetti-base.md), che spiega le parole
che incontrerai (classe, metodo, `final`, lambda, Swing…).

## Ordine di lettura consigliato

| # | Documento | Contenuto | Perché in quest'ordine |
|---|---|---|---|
| 1 | [1-app.md](1-app.md) | `Main`, `AppContext` | è il punto di partenza del programma |
| 2 | [2-model.md](2-model.md) | domande, risposte, esami, risultati | è il "vocabolario" usato ovunque |
| 3 | [3-util.md](3-util.md) | calcoli IPv4, testi | funzioni semplici e indipendenti |
| 4 | [5-data.md](5-data.md) | lettura dei file, generatore, salvataggi | da dove arrivano i dati |
| 5 | [4-engine.md](4-engine.md) | costruzione e correzione degli esami | la logica centrale |
| 6 | [6-ui-base.md](6-ui-base.md) | attrezzi grafici, finestra principale | come è costruita la grafica |
| 7 | [7-ui-menu.md](7-ui-menu.md) | schede del menu, esame personalizzato | le schermate prima dell'esame |
| 8 | [8-ui-esame.md](8-ui-esame.md) | schermo d'esame, domanda, report | le schermate durante e dopo |

## Come leggere un blocco

```java
41  public static long usableHosts(int prefix) {
```

- Il numero a sinistra è il **numero di riga** nel file sorgente (lo stesso che vedi in VS Code).
- Sotto il blocco trovi la spiegazione. Se un blocco contiene più righe, la spiegazione le descrive
  nell'ordine in cui compaiono.
- I riferimenti tipo "[algoritmo 4](../08-algoritmi.md)" portano al flowchart del procedimento.

## Tenere aggiornata questa sezione

I numeri di riga corrispondono alla versione del codice nello stesso commit. Quando modifichi il
codice, aggiorna le spiegazioni e rigenera i file con lo script incluso:

```bash
cd docs/riga-per-riga/_generatore
python3 genera.py ../../../src/main/java/com/lordoc .. annotazioni/*.txt
```

- Le spiegazioni si scrivono nei file `annotazioni/*.txt`. Ogni file Java inizia con
  `@@ percorso/File.java | descrizione`; ogni blocco inizia con `>> ` seguito dall'**inizio** di una
  riga di codice (l'"ancora"), e sotto c'è la spiegazione.
- Lo script trova da solo i numeri di riga e mostra tutte le righe tra un'ancora e la successiva, così
  nessuna riga resta scoperta.
- Se un'ancora non si trova più (perché hai cambiato quella riga), lo script lo segnala con il nome del
  file e termina con errore: aggiorna l'ancora e la spiegazione.
- Serve Python 3 (solo per rigenerare la documentazione, non per il programma).
