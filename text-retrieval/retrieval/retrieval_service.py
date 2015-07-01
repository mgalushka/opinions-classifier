import web
from readability.readability import Document
import urllib

urls = (
    '/(.*)', 'text'
)
app = web.application(urls, globals())

class text:
    def GET(self, url):
        print("Retrieving text for {0}".format(url))
        html = urllib.urlopen(url).read()
        return Document(html).summary()


if __name__ == "__main__":
    app.run()