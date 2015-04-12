#!/bin/bash   

/usr/bin/nc localhost 8093 | echo 0

kill `ps aux | grep TweetPublishScheduler | awk '{print $2}'`