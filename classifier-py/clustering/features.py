import re
import nltk
from nltk.stem.snowball import SnowballStemmer


# noinspection PyMethodMayBeStatic
class StemExistsFeaturesExtractor:
    def __init__(self):
        # manually exclude all punctuation signs
        self.excluded = {"'", "!", ".", ",", ":", "-", "\"", "@", "/", "\\"}
        self.stemmer = SnowballStemmer("english")
        self.mention = re.compile(r'^@.*')

    def clean_urls(self, text):
        """
        Removes all URLS from text
        http://stackoverflow.com/a/6883094/2075157
        """
        return self.clean_http_remains(
            re.sub(r'http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+', '', text,
                   flags=re.MULTILINE))

    def clean_http_remains(self, text):
        return re.sub(r'http[\S]*', '', text, flags=re.MULTILINE)

    def clean_mentions(self, text):
        return re.sub(r'\B@\w+', '', text, flags=re.MULTILINE)

    def strip_non_ascii(self, string):
        """
        Returns the string without non ASCII characters
        """
        stripped = (c for c in string if 0 < ord(c) < 127)
        return ''.join(stripped)

    def extract(self, sentence):
        """
        Extracts features from input sentence:
        features for sentence is set of unique stemmed words
        """
        raw_text = self.clean_mentions(self.clean_urls(sentence))
        words = nltk.wordpunct_tokenize(raw_text)
        features = set()
        for w in words:
            w = self.strip_non_ascii(w).lower().strip()
            if w and w not in self.excluded:
                w = self.stemmer.stem(w).encode("utf-8")
                features.add(w)
        return features