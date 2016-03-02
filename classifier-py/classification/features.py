import re
from bs4 import BeautifulSoup
from nltk.corpus import stopwords
from sklearn.feature_extraction.text import (
    CountVectorizer,
    TfidfTransformer,
    TfidfVectorizer,
)


def top_words_features(top_words, txt):
    document_words = set(txt)
    features = {}
    for word in top_words:
        features[u'contains({0})'.format(word)] = (word in document_words)
    return features


def tweet_to_words(document):
    # Function to convert a raw review to a string of words
    # The input is a single string (a raw movie review), and
    # the output is a single string (a preprocessed movie review)
    #
    # 1. Remove HTML
    tweet_text = u"".join(document)

    print(u"Word: [{0}]".format(tweet_text))
    review_text = BeautifulSoup(tweet_text).get_text()
    #
    # 2. Remove non-letters
    letters_only = re.sub("[^a-zA-Z]", " ", tweet_text)
    #
    # 3. Convert to lower case, split into individual words
    words = letters_only.lower().split()
    #
    # 4. In Python, searching a set is much faster than searching
    #   a list, so convert the stop words to a set
    stops = set(stopwords.words("english"))
    #
    # 5. Remove stop words
    meaningful_words = [w for w in words if not w in stops]
    #
    # 6. Join the words back into one string separated by space,
    # and return the result.
    features = {}
    for word in meaningful_words:
        features[word] = True
    return features


def bag_of_words(documents):
    # Initialize the "CountVectorizer" object, which is scikit-learn's
    # bag of words tool.
    vectorizer = CountVectorizer(
            analyzer="word",
            tokenizer=None,
            preprocessor=None,
            stop_words=stopwords.words("english"),
            max_features=5000,
    )

    # fit_transform() does two functions: First, it fits the model
    # and learns the vocabulary; second, it transforms our training data
    # into feature vectors. The input to fit_transform should be a list of
    # strings.
    train_data_features = vectorizer.fit_transform(documents)

    # Numpy arrays are easy to work with, so convert the result to an
    # array
    return train_data_features.toarray()


def tf_idf_words(documents):
    counts = bag_of_words(documents)
    transformer = TfidfTransformer()
    return transformer.fit_transform(counts).toarray()


def tf_idf_vectorizer(documents):
    vectorizer = TfidfVectorizer(min_df=1)
    vectorizer.fit_transform(documents)
    return transformer.fit_transform(counts).toarray()
