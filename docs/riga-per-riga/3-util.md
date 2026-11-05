# Riga per riga — pacchetto `util`

Funzioni di utilità senza stato: calcoli sugli indirizzi IPv4 (`IpUtils`) e trattamento dei testi (`Text`). Descrizione generale: [07 - app e util](../07-classi-app-util.md).

**File in questo documento:** [`IpUtils.java`](#iputilsjava) · [`Text.java`](#textjava)

---

## IpUtils.java

`src/main/java/com/ccnaexamlab/util/IpUtils.java` — calcoli IPv4 con operazioni sui bit.

**Righe 1–3**

```java
1  package com.ccnaexamlab.util;
2  
3  /** Funzioni di utilità per indirizzi IPv4 (usate dal calcolatore e dal generatore di subnetting). */
```

Pacchetto e commento.

**Righe 4–6**

```java
4  public final class IpUtils {
5  
6      private IpUtils() { }
```

Classe di sole funzioni statiche; il costruttore privato impedisce di crearne oggetti.

**Righe 8–13**

```java
 8      /** Converte "a.b.c.d" in un long senza segno. */
 9      public static long toLong(String ip) {
10          String[] p = ip.trim().split("\\.");
11          if (p.length != 4) {
12              throw new IllegalArgumentException("Indirizzo IPv4 non valido: " + ip);
13          }
```

`toLong`: divide il testo sui punti (`split("\\.")`; il punto va "protetto" con `\\` perché nelle espressioni regolari significa "qualsiasi carattere"). Se le parti non sono 4 segnala errore.

**Righe 14–28**

```java
14          long v = 0;
15          for (String s : p) {
16              int o;
17              try {
18                  o = Integer.parseInt(s);
19              } catch (NumberFormatException e) {
20                  throw new IllegalArgumentException("Ottetto non valido: " + s);
21              }
22              if (o < 0 || o > 255) {
23                  throw new IllegalArgumentException("Ottetto fuori range: " + s);
24              }
25              v = (v << 8) | o;
26          }
27          return v;
28      }
```

Per ogni parte: la converte in numero (`Integer.parseInt`), segnalando un errore se non è un numero o se è fuori da 0-255. Poi `v = (v << 8) | o` sposta il valore accumulato di 8 bit a sinistra (moltiplica per 256) e inserisce il nuovo ottetto nei bit liberi: dopo quattro giri `v` contiene l'intero indirizzo.

**Righe 30–32**

```java
30      public static String toIp(long v) {
31          return ((v >> 24) & 255) + "." + ((v >> 16) & 255) + "." + ((v >> 8) & 255) + "." + (v & 255);
32      }
```

Operazione inversa: per ogni ottetto sposta a destra di 24, 16, 8 e 0 bit e tiene gli ultimi 8 (`& 255`), poi unisce con i punti.

**Righe 34–37**

```java
34      public static long mask(int prefix) {
35          checkPrefix(prefix);
36          return prefix == 0 ? 0 : (0xFFFFFFFFL << (32 - prefix)) & 0xFFFFFFFFL;
37      }
```

Maschera di `prefix` bit: parte da 32 bit a 1 (`0xFFFFFFFFL`), li sposta a sinistra di (32 − prefisso) posizioni (entrano zeri a destra) e taglia a 32 bit. Il caso /0 è trattato a parte (maschera tutta a zero). `checkPrefix` verifica che il prefisso sia tra 0 e 32.

**Riga 39**

```java
39      public static String maskString(int prefix) { return toIp(mask(prefix)); }
```

Maschera in forma puntata (es. 255.255.255.192).

**Riga 41**

```java
41      public static String wildcardString(int prefix) { return toIp(~mask(prefix) & 0xFFFFFFFFL); }
```

Wildcard = maschera con i bit invertiti (`~`), tagliata a 32 bit.

**Riga 43**

```java
43      public static long network(long ip, int prefix) { return ip & mask(prefix); }
```

Indirizzo di rete: AND bit a bit tra indirizzo e maschera (i bit di host diventano 0).

**Riga 45**

```java
45      public static long broadcast(long ip, int prefix) { return network(ip, prefix) | (~mask(prefix) & 0xFFFFFFFFL); }
```

Broadcast: rete OR wildcard (i bit di host diventano 1).

**Righe 47–57**

```java
47      /** Host utilizzabili: /31 = 2 (RFC 3021, link punto-punto), /32 = 1. */
48      public static long usableHosts(int prefix) {
49          checkPrefix(prefix);
50          if (prefix == 32) {
51              return 1;
52          }
53          if (prefix == 31) {
54              return 2;
55          }
56          return (1L << (32 - prefix)) - 2;
57      }
```

`usableHosts`: per /32 un solo indirizzo, per /31 due (link punto-punto senza rete né broadcast), altrimenti 2^(bit di host) − 2. `1L << n` è il modo veloce di calcolare 2^n.

**Righe 59–67**

```java
59      /** Converte una maschera puntata (255.255.255.0) in prefisso, verificando che sia contigua. */
60      public static int prefixFromMask(String mask) {
61          long m = toLong(mask);
62          int prefix = Long.bitCount(m);
63          if (mask(prefix) != m) {
64              throw new IllegalArgumentException("Maschera non contigua: " + mask);
65          }
66          return prefix;
67      }
```

`prefixFromMask`: conta i bit a 1 (`Long.bitCount`) e controlla che la maschera ricostruita da quel numero sia identica all'originale; se non lo è i bit a 1 non erano tutti consecutivi (maschera non valida).

**Righe 69–85**

```java
69      /** Classe "classful" dell'indirizzo (A-E). */
70      public static char ipClass(long ip) {
71          int first = (int) ((ip >> 24) & 255);
72          if (first < 128) {
73              return 'A';
74          }
75          if (first < 192) {
76              return 'B';
77          }
78          if (first < 224) {
79              return 'C';
80          }
81          if (first < 240) {
82              return 'D';
83          }
84          return 'E';
85      }
```

`ipClass`: estrae il primo ottetto e lo confronta con i limiti storici delle classi (A < 128, B < 192, C < 224, D < 240, altrimenti E).

**Righe 87–92**

```java
87      /** true se l'indirizzo appartiene agli spazi privati RFC 1918. */
88      public static boolean isPrivate(long ip) {
89          return network(ip, 8) == toLong("10.0.0.0")
90              || network(ip, 12) == toLong("172.16.0.0")
91              || network(ip, 16) == toLong("192.168.0.0");
92      }
```

`isPrivate`: verifica se l'indirizzo, "tagliato" a /8, /12 o /16, coincide con 10.0.0.0, 172.16.0.0 o 192.168.0.0.

**Righe 94–103**

```java
 94      public static String toBinary(long v) {
 95          StringBuilder sb = new StringBuilder();
 96          for (int i = 31; i >= 0; i--) {
 97              sb.append((v >> i) & 1);
 98              if (i % 8 == 0 && i > 0) {
 99                  sb.append('.');
100              }
101          }
102          return sb.toString();
103      }
```

`toBinary`: per ogni bit dal 31° allo 0 aggiunge "0" o "1" (`(v >> i) & 1` estrae il bit i) e un punto ogni 8 bit, tranne alla fine.

**Righe 105–110**

```java
105      private static void checkPrefix(int prefix) {
106          if (prefix < 0 || prefix > 32) {
107              throw new IllegalArgumentException("Prefisso non valido: /" + prefix);
108          }
109      }
110  }
```

Controllo comune: un prefisso fuori da 0-32 genera un errore con un messaggio chiaro.

---

## Text.java

`src/main/java/com/ccnaexamlab/util/Text.java` — utilità per i testi.

**Righe 1–6**

```java
1  package com.ccnaexamlab.util;
2  
3  /** Piccole utilità per testo e formattazione. */
4  public final class Text {
5  
6      private Text() { }
```

Pacchetto, commento e costruttore privato (solo funzioni statiche).

**Righe 8–23**

```java
 8      public static String escapeHtml(String s) {
 9          if (s == null) {
10              return "";
11          }
12          StringBuilder sb = new StringBuilder(s.length() + 16);
13          for (char c : s.toCharArray()) {
14              switch (c) {
15                  case '<': sb.append("&lt;"); break;
16                  case '>': sb.append("&gt;"); break;
17                  case '&': sb.append("&amp;"); break;
18                  case '"': sb.append("&quot;"); break;
19                  default: sb.append(c);
20              }
21          }
22          return sb.toString();
23      }
```

Sostituisce i caratteri speciali dell'HTML (`< > & "`) con i loro codici, così un testo come `<vlan>` viene mostrato e non interpretato. Usa uno `StringBuilder` leggermente più grande del testo e uno `switch` carattere per carattere.

**Righe 25–31**

```java
25      /** Converte testo semplice in HTML, rendendo `codice` in monospace e le righe vuote in paragrafi. */
26      public static String toHtml(String s) {
27          String e = escapeHtml(s);
28          e = e.replaceAll("`([^`]+)`", "<code>$1</code>");
29          e = e.replaceAll("\\*\\*([^*]+)\\*\\*", "<b>$1</b>");
30          return e.replace("\n\n", "<br><br>").replace("\n", "<br>");
31      }
```

`toHtml`: prima protegge i caratteri speciali, poi con due espressioni regolari trasforma `` `x` `` in `<code>x</code>` e `**x**` in `<b>x</b>` (`$1` è il testo catturato tra le parentesi), infine converte gli a capo in `<br>`.

**Righe 33–41**

```java
33      public static String formatDuration(int seconds) {
34          int h = seconds / 3600;
35          int m = (seconds % 3600) / 60;
36          int s = seconds % 60;
37          if (h > 0) {
38              return String.format("%d:%02d:%02d", h, m, s);
39          }
40          return String.format("%02d:%02d", m, s);
41      }
```

Converte secondi in `h:mm:ss` o `mm:ss`: ore = secondi ÷ 3600, minuti = resto ÷ 60, secondi = resto di 60 (`%`). `%02d` scrive un numero su due cifre con lo zero davanti.

**Righe 43–54**

```java
43      /** Elimina righe vuote iniziali e spazi finali, preservando l'indentazione interna. */
44      public static String trimBlock(String s) {
45          if (s == null) {
46              return "";
47          }
48          String r = s.stripTrailing();
49          while (r.startsWith("\n") || r.startsWith("\r")) {
50              r = r.substring(1);
51          }
52          return r;
53      }
54  }
```

`trimBlock`: `stripTrailing` toglie spazi e a capo in fondo; il ciclo `while` toglie gli a capo all'inizio uno alla volta. Non usa `trim()` perché eliminerebbe anche gli spazi iniziali della prima riga, rovinando l'allineamento degli exhibit.
