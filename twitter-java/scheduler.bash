#!/bin/bash   

CLASSPATH="./target/dependency/*:./target/opinions-tweets-classifier-1.0.jar"
JAVA_MEM="-Xms256M -Xmx512M"
JAVA_JMX="" #"-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
JAVA_GC="-verbose:gc"

nohup java -cp ${CLASSPATH} ${JAVA_MEM} ${JAVA_GC} ${JAVA_JMX} com.maximgalushka.classifier.twitter.service.TweetPublishScheduler > scheduler.log 2>&1 &
echo 0
