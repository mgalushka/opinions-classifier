import features
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

# returns all non-stop words from corpus
def get_top_words():
    all_words = nltk.FreqDist(
        w.lower() for w in decisions.words()
        if not w.lower() in stopwords.words('english')
    )
    word_features = list(all_words)[:500]
    nltk.FreqDist.pprint(all_words, 500)
    return word_features


print(decisions.categories())
documents = [(list(decisions.words(fileid)), category)
             for category in decisions.categories()
             for fileid in decisions.fileids(category)]

#random.shuffle(documents)
print (documents)

pos_features = [(features.tweet_to_words(d), c) for (d, c) in documents if c == 'pos']
neg_features = [(features.tweet_to_words(d), c) for (d, c) in documents if c == 'neg']

random.shuffle(pos_features)
random.shuffle(neg_features)

chosen_features_200 = pos_features[:100] + neg_features[:100]
random.shuffle(chosen_features_200)

featuresets = chosen_features_200

size = 200

train_set, test_set = featuresets[size / 2:], featuresets[:size / 2]
classifier = nltk.DecisionTreeClassifier.train(train_set)
print("Decision trees accuracy = [{0}]".format(
    nltk.classify.accuracy(classifier, test_set)
))

print(classifier.pseudocode(depth=15))
