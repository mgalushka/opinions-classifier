#!/bin/bash   

nohup java -cp ./target/dependency/*:./target/opinions-tweets-classifier-1.0.jar -Xms512m -Xmx512m com.maximgalushka.classifier.twitter.service.MainServiceStart > out.log 2>&1 &
echo 0
