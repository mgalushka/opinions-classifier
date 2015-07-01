import urllib
import web
from readability.readability import Document


urls = (
    '/(.*)', 'retrieve'
)
app = web.application(urls, globals())

class retrieve:
    def GET(self, url):
        print("Retrieving text for {0}".format(url))
        html = urllib.urlopen(url).read()
        return Document(html).summary()


if __name__ == "__main__":
    app.run()