import sys
import codecs
import pickle
from nltk import NaiveBayesClassifier
from collections import defaultdict


class NaiveBayesNews:
    def __init__(self, _extractor, labeled_path=None):
        self.labeled_file = codecs.open(labeled_path, "r", "utf-8")
        self.classifier = None
        self.extractor = _extractor
        if labeled_path:
            self.train()

    def train(self):
        """
        trains classifier on input labeled data
        """
        print "Start training"
        train_set = []
        l = "x"
        while l:
            l = self.labeled_file.readline()
            s = l.find(",")
            label = l[:s]
            feature = self.extractor.extract(l[s + 2:])
            train_set.append((feature, label))
        self.classifier = NaiveBayesClassifier.train(train_set)

    def store(self, out_path):
        """
        stores classifier in file by path
        """
        if self.classifier:
            print "Storing to %s" % out_path
            f = open(out_path, "wb")
            pickle.dump(self.classifier, f)
            f.close()
        else:
            print "Classifier is None, nothing to store"

    def load(self, from_path):
        """
        :return: trained classifiers stored in file
        """
        print "Loading from %s" % from_path
        f = open(from_path)
        self.classifier = pickle.load(f)
        f.close()

    def classify(self, features):
        return self.classifier.classify(features)

    def print_details(self):
        print self.classifier.show_most_informative_features()

