histo = {}
for l in open('top-1000-english-words.txt'):
    ln = len(l.strip())
    histo[ln] = histo.get(ln, 0) + 1
    
keys = histo.keys()
keys.sort()
for k in keys:
    print k, histo[k]
    