#!/bin/bash   

nc localhost 8091 | echo 0

kill `ps aux | grep TweetPublishScheduler | awk '{print $2}'`

exit 0