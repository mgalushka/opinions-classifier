# -*- coding: utf-8 -*-
import sys
reload(sys)
sys.setdefaultencoding("utf-8")

import features
import codecs

if __name__ == "__main__":
    from_file = codecs.open(sys.argv[1], "r", "utf-8")
    to_file = codecs.open(sys.argv[2], "wb", "utf-8")
    f = features.StemExistsFeaturesExtractor()
    line = "x"
    while line:
        line = from_file.readline().encode("utf-8")
        raw_tweet_text = f.clean_all(line).strip()
        if raw_tweet_text:
            to_file.write(raw_tweet_text.encode("utf-8"))
    to_file.flush()
    to_file.close()
    from_file.close()