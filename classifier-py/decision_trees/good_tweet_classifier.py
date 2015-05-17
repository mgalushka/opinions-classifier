import nltk
import os
from nltk.corpus.reader.plaintext import (
    CategorizedPlaintextCorpusReader,
)

decisions = CategorizedPlaintextCorpusReader(
    '{0}/projects/opinions-classifier/'
    'classifier-py/data/tweets_publish_choice'.format(
        os.getenv("HOME")
    ),
    r'.*\.txt',
    cat_pattern=r'(\w+)/*',
    encoding='ascii',
)
all_words = nltk.FreqDist(w.lower() for w in decisions.words())
word_features = list(all_words)[:2000]

print(word_features)


def text_features(txt):
    document_words = set(txt)
    features = {}
    for word in word_features:
        features['contains({})'.format(word)] = (word in document_words)
    return features


def prepare_labeled_data():
    pass

