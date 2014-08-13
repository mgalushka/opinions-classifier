import codecs
from collections import defaultdict
import nltk
import re


class WordExistsFeaturesExtractor:
    def __init__(self, full_data_file):
        self.excluded = set()
        self.preprocess(full_data_file)

    def clean(self, text):
        # http://stackoverflow.com/a/6883094/2075157
        return re.sub(r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', text, '', flags=re.MULTILINE)

    def preprocess(self, full_data_file):
        labeled_file = codecs.open(full_data_file, "r", "utf-8")
        all_word_counts = defaultdict(int)
        line = "x"
        while line:
            line = self.clean(labeled_file.readline().encode("utf-8"))
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
        raw_text = self.clean(sentence.encode("utf-8"))
        words = nltk.wordpunct_tokenize(raw_text)
        map = defaultdict(int)
        for w in words:
            map[w] += 1
        features = {}
        for k in map.iterkeys():
            if k and k not in self.excluded:
                features["contains('%s')" % k] = True
        return features

