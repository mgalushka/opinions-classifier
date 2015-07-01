# -*- coding: utf-8 -*-

import urllib
import web
from readability.readability import Document


urls = (
    '/(.*)', 'retrieve'
)
app = web.application(urls, globals())

class retrieve:
    def GET(self, request):
        url = web.input()['text']
        print("Retrieving text for {0}".format(url))
        html = urllib.urlopen(url).read().decode('utf-8')
        return Document(html).summary()

if __name__ == "__main__":
    app.run()