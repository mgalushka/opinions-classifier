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

# now - hardcoded - we need to extend later to get them from DB
accounts = range(1, 3)

def load_svm(handler):
    if not web.svm:
        web.svm = {}
        version = sys.argv[2]
        for account_id in accounts:
            print("Loading SVM model, account {1}, version {0}".format(account_id, version))
            web.svm[account_id] = joblib.load('../model/svm/account_{0}/{1}/svm.pkl'.format(account_id, version))
            print("Loaded SVM")
    return handler()


class classify(object):
    def GET(self, request):
        web.header('Access-Control-Allow-Origin', '*')
        if not web.svm:
            return 'error'

        text = web.input(_unicode=False)['text'].decode('utf-8', 'ignore')
        account_id = int(web.input(_unicode=False)['id'].decode('utf-8', 'ignore'))
        account_id = account_id or 1
        print(u"Classifying text [{0}]".format(text))
        features_list = [features.tweet_to_words(text)]
        if features_list and len(features_list) > 0:
            try:
                labels = web.svm[account_id].classify_many(features_list)
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


