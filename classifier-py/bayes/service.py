import sys
import web
import urllib
import WordExistsFeatures
import Classifier

urls = (
    '/classify', 'hello'
)
app = web.application(urls, globals())

# loading trained classifier from configuration file
extractor = WordExistsFeatures.WordExistsFeaturesExtractor()
classifier = Classifier.NaiveBayesNews(extractor)
classifier.load(sys.argv[2])


class hello:
    def GET(self):
        inpt = web.input()
        if not inpt.tweet:
            return ""
        t = urllib.unquote(inpt.tweet)
        features = extractor.extract(t)
        c = classifier.classify(features)
        print "%s -> %s" % (c, t)
        return c


if __name__ == "__main__":
    app.run()
