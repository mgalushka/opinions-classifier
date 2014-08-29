package com.maximgalushka.classifier.twitter.classify.python

/**
 *
 * @author Maxim Galushka
 */


def br = new BufferedReader(new FileReader("D:\\projects\\classifier\\200-out.arff"));
def out = new PrintWriter(
        new BufferedWriter(new FileWriter("D:\\projects\\classifier\\200-out.cleaned.arff", true)));

def line;
while ((line = br.readLine()) != null) {
    out.printf("%s,%s\n", input.name().toLowerCase(), line);
    out.flush();
}
out.close();