<?php
include 'config.php';

function connect(){
    global $MYSQL_SERVER, $MYSQL_USER, $MYSQL_PASSWORD, $MYSQL_DATABASE;
    $link = mysql_connect($MYSQL_SERVER, $MYSQL_USER, $MYSQL_PASSWORD);
    if (!$link) {
        die('Could not connect: ' . mysql_error());
    }
    mysql_set_charset('UTF-8', $link);
    if (!mysql_select_db($MYSQL_DATABASE, $link)) {
        echo 'Could not select database';
        exit;
    }
    return $link;
}
?>