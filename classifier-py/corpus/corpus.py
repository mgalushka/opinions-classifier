import nltk
from nltk.corpus import brown

brown_news_tagged = brown.tagged_words(categories='news')
tag_fd = nltk.FreqDist(word for (word, tag) in brown_news_tagged)
for key in tag_fd.keys():
    print "%s -> %d" % (key, tag_fd[key])