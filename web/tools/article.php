<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';

$tweet_id = null;
if (array_key_exists("tweet_id", $_GET)) {
  $tweet_id = $_GET['tweet_id'];
}

if ($tweet_id === null) {
  echo "";
  exit();
}

$link = connect();
echo(DB::queryFirstRow(
  sprintf("
    SELECT article
    FROM tweets_all
    WHERE id = %s",
    $tweet_id
  )
)['article']);
?>