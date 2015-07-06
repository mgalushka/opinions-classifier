# -*- coding: utf-8 -*-

import web
import features
from sklearn.externals import joblib

urls = (
    '/(.*)', 'classify'
)
app = web.application(urls, globals())

svm = joblib.load('../model/svm/0.1/svm.pkl')

class classify:
    def GET(self, request):
        web.header('Access-Control-Allow-Origin', '*')
        text = web.input()['text']
        print("Classifying text [{0}]".format(text))
        return svm.classify_many([features.tweet_to_words(text)])[0]

if __name__ == "__main__":
    app.run()

