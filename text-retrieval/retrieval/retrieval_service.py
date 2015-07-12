# -*- coding: utf-8 -*-

import urllib2
import web
from readability.readability import Document


urls = (
    '/(.*)', 'retrieve'
)
app = web.application(urls, globals())


class retrieve:
    def GET(self, request):
        web.header('Access-Control-Allow-Origin', '*')
        url = web.input()['text']
        print("Retrieving text for {0}".format(url))
        request = urllib2.Request(url)
        request.add_header(
            'User-Agent',
            'Mozilla/5.0 (Windows NT 6.3; WOW64; rv:39.0) Gecko/20100101 Firefox/39.0'
        )

        opener = urllib2.build_opener()
        html = opener.open(request).read().decode('utf-8', 'ignore')
        return Document(html).summary()


if __name__ == "__main__":
    app.run()