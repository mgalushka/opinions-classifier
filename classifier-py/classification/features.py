import re
from bs4 import BeautifulSoup
from nltk.corpus import stopwords

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
    tweet_text = u" ".join(document)

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

#print(tweet_to_words('Let\'s try to test this. !What is this?@'))