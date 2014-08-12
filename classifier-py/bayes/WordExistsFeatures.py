from collections import defaultdict


class WordExistsFeaturesExtractor:
    def __init__(self):
        pass

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
            if k:
                features["contains('%s')" % k] = True
        return features
