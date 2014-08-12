__author__ = 'mgalushka'

import sys
from nltk import NaiveBayesClassifier
import web
from collections import defaultdict
import pickle
import urllib

urls = (
    '/classify', 'hello'
)
app = web.application(urls, globals())
f = open(sys.argv[2])
classifier = pickle.load(f)
f.close()

def sentence_features(sentence):
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

class hello:
    def GET(self):
        inpt = web.input()
        if not inpt.tweet:
            return ""
        t = urllib.unquote(inpt.tweet)
        feature = sentence_features(t)
        c = classifier.classify(feature)
        print "%s -> %s" % (c, t)
        return c

if __name__ == "__main__":
    app.run()
