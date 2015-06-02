<?php
header('Content-type: text/html; charset=utf-8');
include '../tools/db.php';

$link = connect();

mysqli_free_result($result_average);
mysqli_free_result($result);
mysqli_close($link);
;
?>
