<?php
include '../db.php';

$link = connect();
$negative_samples = DB::query("
    SELECT DISTINCT tweet_cleaned
    FROM tweets_clusters c
      JOIN tweets_all t
        ON c.best_tweet_id = t.id
      LEFT JOIN tweets_scheduled s
        ON t.id = s.id
    WHERE
      s.id IS NULL AND
      c.is_displayed = 0
    "
);

$ROOT = "D:\\projects\\opinions-classifier\\classifier-py\\data\\tweets_publish_choice\\";
$dir = $ROOT . "neg\\";
$counter = 0;
foreach ($negative_samples as $sample) {
    file_put_contents($dir . $counter . '.txt', $sample);
    $counter++;
}

$positive_samples = DB::query("
    select distinct t.tweet_cleaned
    from tweets_scheduled s
	  join tweets_all t
		on s.id = t.id
    "
);

$dir = $ROOT . "pos\\";
$counter = 0;
foreach ($positive_samples as $sample) {
    file_put_contents($dir . $counter . '.txt', $sample);
    $counter++;
}


?>