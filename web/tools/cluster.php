<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';
include 'header.php';

if (!isset($_REQUEST['cluster_id'])) {
    echo 'No cluster_id passed.';
    exit;
}

$cluster_id = $_REQUEST['cluster_id'];
$link = connect();

$sql = sprintf('
    SELECT
        best_tweet_id
    FROM tweets_clusters
    WHERE cluster_id = %s',
    $cluster_id
);
$result = mysqli_query($link, $sql);
$best_tweet = 0;
while ($row = mysqli_fetch_assoc($result)) {
    $best_tweet = $row['best_tweet_id'];
}

$sql = sprintf('
        SELECT
            t.id,
            r.cluster_id,
            t.content_json,
            t.tweet_cleaned,
            t.features,
            DATE_FORMAT(t.created_timestamp, "%%Y-%%m-%%d %%H:%%i") as created_timestamp
        FROM tweets_all t JOIN clusters_runs r
          ON t.id = r.tweet_id
        WHERE r.cluster_id = %s
        LIMIT 1000',
    $cluster_id
);

$result = mysqli_query($link, $sql);

if (!$result) {
    echo "DB Error, could not query the database\n";
    echo 'MySQL Error: ' . mysql_error();
    exit;
}
?>
<div class="page-header">
    <h1>All tweets for cluster
        <small>cluster_id=<?= $cluster_id ?>, best=<?= $best_tweet ?></small>
    </h1>
</div>
<div class="container-fluid"><?
    while ($row = mysqli_fetch_assoc($result)) {
        $tweet_json = preg_replace('/[\x00-\x1F\x80-\xFF]/', '', $row['content_json']);
        $tweet_json_read = preg_replace('/,/', ', ', $tweet_json);
        $features = preg_replace('/,/', ', ', $row['features']);
        //$html_tweet = htmlentities(json_decode($row['content_json'], true)['text']);
        $best = '';
        if ($row['id'] == $best_tweet) $best = '<b>BEST</b>';
        ?>
        <div class="row">
            <div class="col-md-2"><span><?= $row['id'] ?></span></div>
            <div class="col-md-3"><span><?= $tweet_json_read ?></span></div>
            <div class="col-md-3"><span><?= $row['tweet_cleaned'] ?></span></div>
            <div class="col-md-1"><span><?= $best ?></span></div>
            <div class="col-md-2"><span><?= $features ?></span></div>
            <div class="col-md-1"><span><?= $row['created_timestamp'] ?></span></div>
        </div>
    <?php
    }
    ?>
</div>
<?
mysqli_free_result($result);
mysqli_close($link);

include 'footer.php';
?>
