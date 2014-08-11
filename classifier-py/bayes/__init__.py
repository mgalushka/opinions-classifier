import sys
from nltk import NaiveBayesClassifier
from collections import defaultdict
import codecs


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
        features["count('%s')" % k] = map[k]
    return features


features = []
label_probdist = []
feature_probdist = []

train_set = []

f = codecs.open(sys.argv[1], "r", "utf-8")
l = "x"
while l:
    l = f.readline()
    s = l.find(",")
    label = l[:s]
    feature = sentence_features(l[s + 2:])
    print "%s -> %s" % (label, feature)
    train_set.append((feature, label))

print(train_set)
f.close()

classifier = NaiveBayesClassifier.train(train_set)

# test on some real data
classify_file = codecs.open(sys.argv[2], "r", "utf-8")
l = "x"
while l:
    l = classify_file.readline()
    feature = sentence_features(l)
    c = classifier.classify(feature)
    if c == "ukraine" or c == "russia":
        print "%s -> %s" % (c, l)

classify_file.close()

print classifier.show_most_informative_features()

