import codecs
from collections import defaultdict


class WordExistsFeaturesExtractor:
    def __init__(self, full_data_file):
        self.excluded = set()
        self.preprocess(full_data_file)

    def preprocess(self, full_data_file):
        labeled_file = codecs.open(full_data_file, "r", "utf-8")
        words = defaultdict(int)
        l = "x"
        while l:
            l = labeled_file.readline().encode("utf-8")
            s = l.find(",")
            wrds = l[s:].split(" ")
            for w in wrds:
                w = w.lower().strip()
                if w:
                    words[w] += 1
        labeled_file.close()
        T = max(words.values()) / 4
        for w in words:
            if words[w] >= T:
                self.excluded.add(w)

    def extract(self, sentence):
        """
        :param sentence:
        :return: Extracts features from input sentence
        """
        words = sentence.encode("utf-8").split(" ")
        map = defaultdict(int)
        for w in words:
            w = w.lower().strip()
            # exclude URLs
            if w.startswith("http"):
                continue
            map[w] += 1
        features = {}
        for k in map.iterkeys():
            if k and k not in self.excluded:
                features["contains('%s')" % k] = True
        return features

