package com.maximgalushka.classifier.twitter.classify.carrot

/**
 * @since 9/5/2014.
 */


println "http://lol".replaceAll("http://", "");

def G = "\\bht\\b|\\bhtt(\\b|\\s)|\\bhttp\\b|\\bhttp:\\b";

println "http:".replaceAll(G, "");
println "ht".replaceAll(G, "");
println "htt".replaceAll(G, "");
println "http".replaceAll(G, "");

//println "htt:".replaceAll("(htt$)|(htt\\s))", "");
println "http".replaceAll("\\bhtt\\b", "");
