import nltk
from nltk.corpus import movie_reviews
import os
from nltk.corpus.reader.plaintext import PlaintextCorpusReader

#decisions = PlaintextCorpusReader('', '');
all_words = nltk.FreqDist(w.lower() for w in movie_reviews.words())
word_features = list(all_words)[:2000]

def text_features(txt):
    document_words = set(txt)
    features = {}
    for word in word_features:
        features['contains({})'.format(word)] = (word in document_words)
    return features

def prepare_labeled_data():
    pass

