import features
import os
import random
import sys

from nltk.corpus import (
    LazyCorpusLoader,
    CategorizedPlaintextCorpusReader,
)
from nltk.classify.scikitlearn import SklearnClassifier

from sklearn.externals import joblib
from sklearn.metrics import classification_report
from sklearn.svm import LinearSVC

version = sys.argv[1]
limit = int(sys.argv[2])

decisions = LazyCorpusLoader(
    'tweets_publish_choice_{version}'.format(version=version),
    CategorizedPlaintextCorpusReader,
    r'.*\.txt',
    cat_pattern=r'(\w+)/*',
    encoding='utf8',
)

home = os.path.expanduser("~")
path = os.path.join(
    home,
    'nltk_data{s}corpora{s}tweets_publish_choice_{version}'.format(
        version=version,
        s=os.sep,
    )
)

print(decisions.categories())
documents = [(list(decisions.words(fileid)), category)
             for category in decisions.categories()
             for fileid in decisions.fileids(category)
             if os.path.getsize(os.path.join(path, fileid)) > 0]

pos_features = [(features.tweet_to_words(d), c) for (d, c) in documents if c == 'pos']
neg_features = [(features.tweet_to_words(d), c) for (d, c) in documents if c == 'neg']

chosen_features_limit = pos_features[:limit / 2] + neg_features[:limit / 2]
random.shuffle(chosen_features_limit)

featuresets = chosen_features_limit

train_set, test_set = featuresets[limit / 2:], featuresets[:limit / 2]

svm = SklearnClassifier(LinearSVC())
svm.train(train_set)

path = os.path.normpath('../model/svm/{0}/'.format(version))
if not os.path.exists(path):
    os.makedirs(path)

print(u'Saving model to {0}'.format(path))
joblib.dump(svm, os.path.join(path, 'svm.pkl'))

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