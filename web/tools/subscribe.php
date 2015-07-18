<?php
header('Content-type: text/html; charset=utf-8');
include '../tools/db.php';

$email = htmlspecialchars($_POST["email"]);

$link = connect();

DB::insert();

mysqli_free_result($result);
mysqli_close($link);
;

