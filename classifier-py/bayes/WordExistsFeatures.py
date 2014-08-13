import codecs
from collections import defaultdict
import nltk


class WordExistsFeaturesExtractor:
    def __init__(self, full_data_file):
        self.excluded = set()
        self.preprocess(full_data_file)

    def preprocess(self, full_data_file):
        labeled_file = codecs.open(full_data_file, "r", "utf-8")
        all_word_counts = defaultdict(int)
        line = "x"
        while line:
            line = labeled_file.readline().encode("utf-8")
            raw_tweet_text = line[line.find(",") + 1:-1]
            tokens = nltk.wordpunct_tokenize(raw_tweet_text)
            for w in tokens:
                w = w.lower().strip()
                if w:
                    all_word_counts[w] += 1
        labeled_file.close()
        T = max(all_word_counts.values()) / 4
        for w in all_word_counts:
            if all_word_counts[w] >= T:
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

