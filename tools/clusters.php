<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';
include 'header.php';
?>

<?php

$cluster_run_id = $_REQUEST['run_id'];
if (empty($cluster_run_id)) {
    $cluster_run_id = 82;  //magic
}

$link = connect();

$sql = sprintf(
    '-- noinspection SqlNoDataSourceInspection
    SELECT
        count(t.id) as total_tweets
    FROM tweets_clusters c join tweets_all t on c.cluster_id = t.cluster_id
        WHERE cluster_run_id = %d',
    $cluster_run_id
);
$result = mysql_query($sql, $link);
$total_tweets = 0;
while ($row = mysql_fetch_assoc($result)) {
    $total_tweets = $row['total_tweets'];
}

$sql = sprintf(
    '-- noinspection SqlNoDataSourceInspection
        SELECT
            count(1) as total_clusters
        FROM tweets_clusters c
        WHERE cluster_run_id = %d',
    $cluster_run_id
);
$result = mysql_query($sql, $link);
$total_clusters = 0;
while ($row = mysql_fetch_assoc($result)) {
    $total_clusters = $row['total_clusters'];
}

$sql = sprintf(
    '-- noinspection SqlNoDataSourceInspection
        SELECT
            c.cluster_id,
            c.name,
            c.best_tweet_id,
            c.is_displayed,
            c.cluster_run_id,
            c.updated_timestamp,
            count(t.id) as tweets_count
        FROM tweets_clusters c join tweets_all t on c.cluster_id = t.cluster_id
            WHERE cluster_run_id = %d
            GROUP BY cluster_id, name, best_tweet_id, is_displayed, cluster_run_id, updated_timestamp
            ORDER BY count(t.id) desc
            LIMIT 1000',
    $cluster_run_id
);
$result = mysql_query($sql, $link);

?>
<div class="page-header">
    <h1>All latest clusters (total <?= $total_clusters ?>)</h1>
</div>
<div class="container-fluid">
    <?
    while ($row = mysql_fetch_assoc($result)) {
        ?>

        <div class="row-fluid">
        <div class="col-md-1"><?= $row['cluster_id'] ?></div>
        <div class="col-md-1"><?= $row['tweets_count'] ?></div>
        <div class="col-md-1"><?= round(100.0 * $row['tweets_count'] / $total_tweets, 2) ?></div>
        <div class="col-md-9">
            <a href="cluster.php?cluster_id=<?= $row['cluster_id'] ?>">
                <?= $row['name'] ?>
            </a>
        </div>
        </div><?php
    }
    ?>
</div>
<?

mysql_free_result($result);
mysql_close($link);

include 'footer.php';
?>
