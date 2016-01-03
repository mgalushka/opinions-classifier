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