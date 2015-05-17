import nltk
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
size = 1979
all_words = nltk.FreqDist(w.lower() for w in decisions.words())
word_features = list(all_words)[:2000]


def text_features(txt):
    document_words = set(txt)
    features = {}
    for word in word_features:
        features['contains({})'.format(word)] = (word in document_words)
    return features


documents = [(list(decisions.words(fileid)), category)
             for category in decisions.categories()
             for fileid in decisions.fileids(category)]
featuresets = [(text_features(d), c) for (d, c) in documents]

train_set, test_set = featuresets[size / 2:], featuresets[:size / 2]
classifier = nltk.DecisionTreeClassifier.train(train_set)
print("Decision trees accuracy = [{0}]".format(
    nltk.classify.accuracy(classifier, test_set)
))

print(classifier.pseudocode(depth=15))
