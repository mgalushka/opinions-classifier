# -*- coding: utf-8 -*-

import web
import features
import sys
import traceback
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
        if not web.svm:
            return 'error'

        text = web.input()['text'].encode('utf-8', 'ignore')
        print(u"Classifying text [{0}]".format(text))
        features_list = [features.tweet_to_words(text)]
        if features_list and len(features_list) > 0:
            try:
                labels = web.svm.classify_many(features_list)
                if labels and len(labels) > 0:
                    return labels[0]
                else:
                    return 'empty'
            except Exception, e:
                traceback.print_exc()
                return 'error'
        else:
            return 'empty'


if __name__ == "__main__":
    app.add_processor(load_svm)
    app.run()


