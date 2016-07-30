set CLASSPATH=./target/dependency/*;./target/opinions-tweets-classifier-1.0.jar
set JAVA_MEM="-Xms128M -Xmx256M"
set JAVA_JMX="" #"-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9010 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
set JAVA_GC="-verbose:gc"

java -cp %CLASSPATH% com.maximgalushka.classifier.twitter.service.TweetPublishScheduler

