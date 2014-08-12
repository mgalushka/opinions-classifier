import sys
import WordExistsFeatures
import Classifier

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "train.py [labeled_file] [output_file]"
        exit()
    # Train classified on manually labeled data
    extractor = WordExistsFeatures.WordExistsFeaturesExtractor()
    classifier = Classifier.NaiveBayesNews(extractor, sys.argv[1])  # NaiveBayesClassifier.train(train_set)
    classifier.store(sys.argv[1])
    classifier.print_details()


"""
f = codecs.open(sys.argv[1], "r", "utf-8")
l = "x"
while l:
    l = f.readline()
    s = l.find(",")
    label = l[:s]
    feature = extractor.extract(l[s + 2:])
    print "%s -> %s" % (label, feature)
    train_set.append((feature, label))

print(train_set)
f.close()

# test on some real data
classify_file = codecs.open(sys.argv[2], "r", "utf-8")
l = "x"
while l:
    l = classify_file.readline()
    feature = extractor.extract(l)
    c = classifier.classify(feature)
    if c == "ukraine" or c == "russia":
        print "%s -> %s" % (c, l)

classify_file.close()

print classifier.show_most_informative_features()
"""

