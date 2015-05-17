import nltk
import os
from nltk.corpus import LazyCorpusLoader
from nltk.corpus.reader.plaintext import (
    CategorizedPlaintextCorpusReader,
)

decisions = LazyCorpusLoader(
    'tweets_publish_choice',
    CategorizedPlaintextCorpusReader,
    r'.*\.txt',
    cat_pattern=r'(\w+)/*',
    encoding='utf8',
)
print(decisions.words())
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
