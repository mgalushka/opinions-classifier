import nltk
import os
from nltk.corpus.reader.plaintext import (
    CategorizedPlaintextCorpusReader,
)

decisions = CategorizedPlaintextCorpusReader(
    '~/MainFolder/',
    r'.*\.txt',
    cat_pattern=r'[0-100]_(\w+)\.txt'
)
all_words = nltk.FreqDist(w.lower() for w in decisions.words())
word_features = list(all_words)[:2000]

def text_features(txt):
    document_words = set(txt)
    features = {}
    for word in word_features:
        features['contains({})'.format(word)] = (word in document_words)
    return features

def prepare_labeled_data():
    pass

