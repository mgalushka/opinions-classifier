<?php
header('Content-type: text/html; charset=utf-8');
include '../tools/db.php';

$email = htmlspecialchars($_POST["email"]);

DB::insert('subscribe', array(
  'email' => $email
));
;
?>OK
