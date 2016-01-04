## Regenerating SSL certificate

From letencrypt install directory:

```
./letsencrypt-auto certonly -a standalone -d api.lightbot.co --text -vv
```

## Apache configuration

Apache config located here (Ubuntu): `/etc/apache2/apache2.conf`

```
<VirtualHost *:80>
   ServerName lightbot.co
   Redirect permanent / https://lightbot.co/
</VirtualHost>
<VirtualHost *:443>
    ServerName lightbot.co
    ProxyPass /easy http://localhost:8092/
    ProxyPassReverse /easy http://localhost:8092/
    SSLEngine on
    SSLCertificateFile "${LIGTHBOT.CO_CERT_ROOT}/fullchain.pem"
    SSLCertificateKeyFile "${LIGTHBOT.CO_CERT_ROOT}/privkey.pem"
    SSLCertificateChainFile "${LIGTHBOT.CO_CERT_ROOT}/chain.pem"
</VirtualHost>
```


## Installation

`sudo apt-get install php5 libapache2-mod-php5 php5-mcrypt`

We need to have `short_open_tag = On` in php.ini directive.
 
on Ubuntu it can be located with small script:

```
<?php
phpinfo();
?>
```

## Logs

`/var/log/apache2/error.log`