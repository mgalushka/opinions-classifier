from nltk import NaiveBayesClassifier
from collections import defaultdict


def gender_features(sentence):
    """
    :param sentence:
    :return: Extracts features from input sentence
    """
    words = sentence.split(" ")
    map = defaultdict(int)
    for w in words:
        w = w.strip()
        map[w] += 1
    features = {}
    for k in map.iterkeys():
        features["count('%s')" % k] = map[k]
    return features


features = []
label_probdist = []
feature_probdist = []

train_set = []

f = file("D:/projects/opinions-classifier/twitter-java/data/data.labeled.txt", "r")
l = f.readline()
print gender_features(l)

f.close()

"""
classifier = NaiveBayesClassifier.train(train_set)
b = NaiveBayesClassifier(label_probdist, feature_probdist)

c = b.classify(input)
"""

