package com.maximgalushka.classifier.clustring

import com.maximgalushka.classifier.clustring.model.Document
import com.maximgalushka.classifier.clustring.realtime.ClusterClassifier
import org.springframework.context.support.ClassPathXmlApplicationContext

/**
 * @author Maxim Galushka
 */

def IN = new BufferedReader(
        new FileReader(
                "D:\\projects\\opinions-classifier\\200.txt"))

def ac =
        new ClassPathXmlApplicationContext(
                "spring/classifier-services.xml"
        );

def ss = (ClusterClassifier) ac.getBean("classifier");
def line = null
def id = 1;
while ((line = IN.readLine()) != null) {
    ss.classify(
            new Document(id++, line, null, null, null, new Date().getTime()))
    println ss.stats.clustersCount
}
IN.close()
println ss.realtimeClusters
System.exit(0);