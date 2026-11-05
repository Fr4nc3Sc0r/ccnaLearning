# Documentazione di Lordoc

Questa cartella contiene la documentazione completa del programma. È scritta pensando a chi **non ha mai
scritto una riga di codice**: ogni concetto tecnico viene prima spiegato con parole semplici e con esempi
della vita quotidiana, e solo dopo viene mostrato come è realizzato nel codice Java.

> I diagrammi (flowchart e UML) sono scritti in **Mermaid**: GitHub e VS Code (con l'estensione
> "Markdown Preview Mermaid Support") li disegnano automaticamente quando apri il file.

## Da dove cominciare

| Se vuoi… | Leggi |
|---|---|
| capire le basi della programmazione necessarie a seguire il resto | [01 - Concetti base](01-concetti-base.md) |
| capire come è organizzato il programma nel suo insieme | [02 - Come funziona il programma](02-come-funziona.md) |
| sapere cosa fa ogni classe, nel dettaglio | [03](03-classi-model.md) · [04](04-classi-data.md) · [05](05-classi-engine.md) · [06](06-classi-ui.md) · [07](07-classi-app-util.md) |
| capire i procedimenti più complicati, con i flowchart | [08 - Algoritmi e flowchart](08-algoritmi.md) |
| aggiungere domande, correggere errori, migliorare il programma | [09 - Manutenzione ed evoluzione](09-manutenzione.md) |
| leggere il codice **riga per riga** con la spiegazione | [Riga per riga](riga-per-riga/README.md) |

## Mappa dei documenti

```
docs/
├── README.md                  ← sei qui
├── 01-concetti-base.md        cos'è un programma, Java, classi, oggetti, Swing... (glossario incluso)
├── 02-come-funziona.md        architettura, avvio, ciclo di vita di un esame, dati salvati, UML generale
├── 03-classi-model.md         il "vocabolario" del programma: domanda, risposta, esame, risultato
├── 04-classi-data.md          lettura delle domande, generatore di subnetting, salvataggio dati
├── 05-classi-engine.md        costruzione degli esami e correzione
├── 06-classi-ui.md            tutte le finestre e i pannelli grafici
├── 07-classi-app-util.md      avvio del programma e funzioni di utilità (calcoli IP, testo)
├── 08-algoritmi.md            flowchart + spiegazione + pseudocodice + Java dei procedimenti complessi
├── 09-manutenzione.md         guida pratica: domande, cheat sheet, build, errori comuni, idee
├── riga-per-riga/             il codice sorgente commentato riga per riga, un file per pacchetto
└── screenshots/               immagini usate nel README principale
```

## Convenzioni usate nella documentazione

- I **nomi di file, classi e comandi** sono scritti `così`.
- Le parole inglesi del codice sono tradotte la prima volta che compaiono (es. *class* = classe).
- Ogni procedimento è descritto in tre livelli:
  1. **In parole semplici** (come lo spiegheresti a voce);
  2. **Pseudocodice** (i passi in forma di elenco quasi-codice, in italiano);
  3. **Java** (il codice reale del programma).
- I numeri di riga citati in `riga-per-riga/` si riferiscono alla versione del codice presente nello
  stesso commit della documentazione.
