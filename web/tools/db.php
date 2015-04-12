<?php
include 'config.php';

function connect(){
    global $MYSQL_SERVER, $MYSQL_USER, $MYSQL_PASSWORD, $MYSQL_DATABASE;
    $link = mysqli_connect($MYSQL_SERVER, $MYSQL_USER, $MYSQL_PASSWORD);
    if (!$link) {
        die('Could not connect: ' . mysql_error());
    }
    mysqli_set_charset($link, 'UTF-8');
    if (!mysqli_select_db($link, $MYSQL_DATABASE)) {
        echo 'Could not select database';
        exit;
    }
    return $link;
}
?>