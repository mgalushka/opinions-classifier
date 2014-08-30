package com.maximgalushka.classifier.twitter.classify

/**
 *
 * @author Maxim Galushka
 */


def br = new BufferedReader(new FileReader("D:\\Dropbox\\NLP\\tweets.stream.sample.cleaned.xml"));
def out = new PrintWriter(
        new BufferedWriter(new FileWriter("D:\\Dropbox\\NLP\\tweets.stream.sample.cleaned.ids.xml", true))) as PrintWriter;

def line;
def counter = 1;
while ((line = br.readLine()) != null) {
    if (line.contains("<document id=\"0\">"))
        out.printf("\t<document id=\"%d\">\n", counter++);
    else
        out.println(line);
    out.flush();
}
out.close();