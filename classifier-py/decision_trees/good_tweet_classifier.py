import random
import nltk
from nltk.corpus import LazyCorpusLoader, stopwords
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
all_words = nltk.FreqDist(w.lower() for w in decisions.words() if not w.lower() in stopwords.words('english'))
word_features = list(all_words)[:50]
nltk.FreqDist.pprint(all_words, 50)


def text_features(txt):
    document_words = set(txt)
    features = {}
    for word in word_features:
        features[u'contains({0})'.format(word)] = (word in document_words)
    return features

print(decisions.categories())
documents = [(list(decisions.words(fileid)), category)
             for category in decisions.categories()
             for fileid in decisions.fileids(category)]

random.shuffle(documents)
print(type(documents))
print(dir(documents))
pos_docs = [(text_features(d), c) for (d, c) in documents if c == 'pos']
neg_docs = [(text_features(d), c) for (d, c) in documents if c == 'neg']

random.shuffle(pos_docs)
random.shuffle(neg_docs)

chosen_docs_200 = pos_docs[:100] + neg_docs[:100]
random.shuffle(chosen_docs_200)

featuresets = [(text_features(d), c) for (d, c) in chosen_docs_200]

train_set, test_set = featuresets[size / 2:], featuresets[:size / 2]
classifier = nltk.DecisionTreeClassifier.train(train_set)
print("Decision trees accuracy = [{0}]".format(
    nltk.classify.accuracy(classifier, test_set)
))

print(classifier.pseudocode(depth=15))
