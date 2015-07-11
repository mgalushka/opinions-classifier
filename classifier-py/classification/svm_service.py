# -*- coding: utf-8 -*-

import web
import features
import sys
from sklearn.externals import joblib

urls = (
    '/(.*)', 'classify'
)
app = web.application(urls, globals())
web.svm = None


def load_svm(handler):
    if not web.svm:
        version = sys.argv[2]
        print("Loading SVM model, version {0}".format(version))
        web.svm = joblib.load('../model/svm/{0}/svm.pkl'.format(version))
        print("Loaded SVM")
    return handler()


class classify(object):

    def GET(self, request):
        web.header('Access-Control-Allow-Origin', '*')
        text = web.input()['text'].decode('utf-8')
        print(u"Classifying text [{0}]".format(text))
        return web.svm.classify_many([features.tweet_to_words(text)])[0]

if __name__ == "__main__":
    app.add_processor(load_svm)
    app.run()


