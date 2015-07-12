import random
import features
import sys

from nltk.corpus import (
    LazyCorpusLoader,
    CategorizedPlaintextCorpusReader,
)
from sklearn.svm import LinearSVC
from nltk.classify.scikitlearn import SklearnClassifier
from sklearn.metrics import classification_report

from sklearn.externals import joblib

version = sys.argv[1]
limit = int(sys.argv[2])

decisions = LazyCorpusLoader(
    'tweets_publish_choice_{version}'.format(version=version),
    CategorizedPlaintextCorpusReader,
    r'.*\.txt',
    cat_pattern=r'(\w+)/*',
    encoding='utf8',
)

print(decisions.categories())
documents = [(list(decisions.words(fileid)), category)
             for category in decisions.categories()
             for fileid in decisions.fileids(category)]

pos_features = [(features.tweet_to_words(d), c) for (d, c) in documents if c == 'pos']
neg_features = [(features.tweet_to_words(d), c) for (d, c) in documents if c == 'neg']

chosen_features_limit = pos_features[:limit / 2] + neg_features[:limit / 2]
random.shuffle(chosen_features_limit)

featuresets = chosen_features_limit

train_set, test_set = featuresets[limit / 2:], featuresets[:limit / 2]

svm = SklearnClassifier(LinearSVC())
svm.train(train_set)

path = '../model/svm/{0}/svm.pkl'.format(version)
print(u'Saving model to {0}'.format(path))
joblib.dump(svm, path)

test_skl = []
t_test_skl = []
for d in test_set:
    test_skl.append(d[0])
    t_test_skl.append(d[1])

# run the classifier on the test test
p = svm.classify_many(test_skl)

# getting a full report
print classification_report(
    t_test_skl,
    p,
    labels=list(set(t_test_skl)),
    target_names=['pos', 'neg']
)