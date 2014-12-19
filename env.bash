export JAVA_HOME=/opt/jdk/jdk1.7.0_67
export M2_HOME=~/apache-maven-3.2.3
export PATH=${JAVA_HOME}/bin:${PATH}:${M2_HOME}/bin


# how to download java from oracle site
# wget --header "Cookie: oraclelicense=accept-securebackup-cookie" http://download.oracle.com/otn-pub/java/jdk/7u67-b01/jdk-7u67-linux-x64.tar.gz


sudo /etc/init.d/mysqld restart

sudo /etc/init.d/memcached restart
