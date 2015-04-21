<?php
require_once 'db.class.php';
include 'config.php';

DB::$host = $MYSQL_SERVER;
DB::$port = $MYSQL_PORT;
DB::$user = $MYSQL_USER;
DB::$password = $MYSQL_PASSWORD;
DB::$dbName = $MYSQL_DATABASE;
DB::$encoding = 'utf8_general_ci';

function connect()
{
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