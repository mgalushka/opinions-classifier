<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';
include 'header.php';
?>

<?php
$cluster_id = $_REQUEST['cluster_id'];
if (empty($cluster_id)) {
    echo 'No cluster_id passed.';
    exit;
}

$link = connect();

$sql = sprintf('SELECT id, cluster_id, content_json, tweet_cleaned, created_timestamp
                FROM tweets_all
                WHERE cluster_id = %s
                LIMIT 1000', $cluster_id);

$result = mysql_query($sql, $link);

if (!$result) {
    echo "DB Error, could not query the database\n";
    echo 'MySQL Error: ' . mysql_error();
    exit;
}
?>
<div class="page-header">
    <h1>All tweets for cluster: <?= $cluster_id ?></h1>
</div>
<div class="container-fluid"><?
    while ($row = mysql_fetch_assoc($result)) {
        $tweet_json = preg_replace('/[\x00-\x1F\x80-\xFF]/', '', $row['content_json']);
        $html_tweet = htmlentities(json_decode($tweet_json, true)['text']);
        ?>
        <div class="row">
            <div class="col-md-1"><span><?= $row['id'] ?></span></div>
            <div class="col-md-5"><span><?= $html_tweet ?></span></div>
            <div class="col-md-5"><span><?= $row['tweet_cleaned'] ?></span></div>
            <div class="col-md-1"><span><?= $row['created_timestamp'] ?></span></div>
        </div>
    <?php
    }
    ?>
</div>
<?
mysql_free_result($result);
mysql_close($link);

include 'footer.php';
?>
