@echo off

set CLASSPATH=./target/dependency/*;./target/opinions-tweets-classifier-1.0.jar
java -cp %CLASSPATH% com.maximgalushka.classifier.twitter.service.TweetPublishScheduler

