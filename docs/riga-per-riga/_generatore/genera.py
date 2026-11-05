"""Genera docs/riga-per-riga/*.md a partire da file di annotazioni.
Formato annotazioni:
  # TITLE: titolo del documento
  # INTRO: testo introduttivo (una riga)
  @@ percorso/File.java | descrizione breve del file
  >> testo iniziale della riga di codice (ancora)
  spiegazione (anche più righe)
Ogni ancora deve comparire, in ordine, all'inizio (dopo gli spazi) di una riga del file.
Il blocco mostrato va dall'ancora fino alla riga prima dell'ancora successiva.
"""
import sys, os, re
SRC = sys.argv[1]; OUT = sys.argv[2]; ann_files = sys.argv[3:]
errors = []
for af in ann_files:
    lines = open(af, encoding='utf-8').read().split('\n')
    title = ''; intro = []; files = []; cur = None; chunk = None
    for l in lines:
        if l.startswith('# TITLE:'): title = l[8:].strip(); continue
        if l.startswith('# INTRO:'): intro.append(l[8:].strip()); continue
        if l.startswith('@@ '):
            path, _, desc = l[3:].partition('|')
            cur = {'path': path.strip(), 'desc': desc.strip(), 'chunks': []}; files.append(cur); chunk = None; continue
        if l.startswith('>> '):
            chunk = {'anchor': l[3:], 'text': []}; cur['chunks'].append(chunk); continue
        if chunk is not None: chunk['text'].append(l)
    out = [f"# {title}", ""]
    out += [i for i in intro] + [""]
    out.append("**File in questo documento:** " + " · ".join(f"[`{os.path.basename(f['path'])}`](#{os.path.basename(f['path']).lower().replace('.', '')})" for f in files))
    out.append("")
    for f in files:
        code = open(os.path.join(SRC, f['path']), encoding='utf-8').read().split('\n')
        if code and code[-1] == '': code = code[:-1]
        pos = 0; starts = []
        for c in f['chunks']:
            a = c['anchor'].strip()
            found = None
            for i in range(pos, len(code)):
                if code[i].strip().startswith(a):
                    found = i; break
            if found is None:
                errors.append(f"{f['path']}: ancora non trovata dopo riga {pos+1}: {a!r}"); found = pos
            starts.append(found); pos = found + 1
        if starts and starts[0] != 0:
            # righe iniziali non coperte
            errors.append(f"{f['path']}: righe 1-{starts[0]} non coperte")
        out += ["---", "", f"## {os.path.basename(f['path'])}", "", f"`src/main/java/com/lordoc/{f['path']}` — {f['desc']}", ""]
        for k, c in enumerate(f['chunks']):
            s = starts[k]; e = (starts[k+1] if k+1 < len(starts) else len(code)) - 1
            while e > s and code[e].strip() == '': e -= 1
            rng = f"Riga {s+1}" if s == e else f"Righe {s+1}–{e+1}"
            out.append(f"**{rng}**")
            out.append("")
            out.append("```java")
            w = len(str(e+1))
            for i in range(s, e+1):
                out.append(f"{str(i+1).rjust(w)}  {code[i]}")
            out.append("```")
            out.append("")
            txt = '\n'.join(c['text']).strip()
            out.append(txt if txt else "_(nessun commento)_")
            out.append("")
        # verifica copertura: nessun buco tra chunk
    name = os.path.splitext(os.path.basename(af))[0] + '.md'
    open(os.path.join(OUT, name), 'w', encoding='utf-8').write('\n'.join(out).rstrip() + '\n')
if errors:
    print('\n'.join(errors)); sys.exit(1)
print('ok')
