# -*- coding: utf-8 -*-
import codecs
from collections import defaultdict
import sys
from time import time
from sklearn import metrics
from sklearn.cluster import KMeans
import features
from sklearn.feature_extraction.text import TfidfVectorizer

if __name__ == "__main__":
    data = []
    data_file = codecs.open(sys.argv[1], "r", "utf-8")
    all_word_counts = defaultdict(int)
    f = features.StemExistsFeaturesExtractor()
    line = "x"
    while line:
        line = data_file.readline().encode("utf-8")
        raw_tweet_text = f.clean_all(line).strip()
        if raw_tweet_text:
            data.append(raw_tweet_text)
    data_file.close()

    vectorizer = TfidfVectorizer()
    vectors = vectorizer.fit_transform(data)

    km = KMeans(n_clusters=3, init='k-means++', max_iter=100, n_init=1)

    print("Clustering sparse data with %s" % km)
    t0 = time()
    km.fit_predict(vectors)
    print("done in %0.3fs" % (time() - t0))
    print()
    """
    print("Homogeneity: %0.3f" % metrics.homogeneity_score(labels, km.labels_))
    print("Completeness: %0.3f" % metrics.completeness_score(labels, km.labels_))
    print("V-measure: %0.3f" % metrics.v_measure_score(labels, km.labels_))
    print("Adjusted Rand-Index: %.3f"
          % metrics.adjusted_rand_score(labels, km.labels_))
    """
    print("Silhouette Coefficient: %0.3f"
          % metrics.silhouette_score(vectors, km.labels_, sample_size=1000))

    print("Top terms per cluster:")
    order_centroids = km.cluster_centers_.argsort()[:, ::-1]
    terms = vectorizer.get_feature_names()
    for i in range(3):
        print("Cluster %d:" % i)
        for ind in order_centroids[i, :10]:
            print(' %s' % terms[ind])
        print()

        # st = f.clean_all("Ukrainian Grain Exports Surge: Ukraine’s grain exports for the period 1 July-14 August totalled 3.3 Mt, up around... http://t.co/mUHR06cJzP").strip()
        #a = km.fit_predict(vectorizer.fit_transform([st]))
        #print a

        # bandwidth = estimate_bandwidth(X, quantile=0.2, n_samples=500)

        # ms = MeanShift(bandwidth=bandwidth, bin_seeding=True)