import sys
import codecs
from collections import defaultdict


if __name__ == "__main__":
    if len(sys.argv) < 1:
        print "train.py [labeled_file] [output_file]"
        exit()
    labeled_file = codecs.open(sys.argv[1], "r", "utf-8")
    words = defaultdict(int)
    l = "x"
    while l:
        l = labeled_file.readline().encode("utf-8")
        s = l.find(",")
        wrds = l[s:].split(" ")
        for w in wrds:
            w = w.strip()
            if w:
                words[w] += 1
    excluded = [i for i in words if words[i] >= 20]
    print excluded