#!/bin/bash

# start apache
sudo arachectl restart

# start mysql
sudo /etc/init.d/mysqld restart

# start memcached
sudo /etc/init.d/memcached restart

chmod 500 start.bash
chmod 500 stop.bash

./start.bash
