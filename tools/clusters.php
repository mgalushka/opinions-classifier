<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';
include 'header.php';
?>

<?php
$link = connect();

$sql = 'SELECT
            count(t.id)
        FROM tweets_clusters c join tweets_all t on c.cluster_id = t.cluster_id
            WHERE cluster_run_id = (select max(cluster_run_id) from tweets_clusters)
            GROUP BY cluster_id
            ORDER BY count(t.id) desc
            LIMIT 1000';
$result = mysql_query($sql, $link);

$sql = 'SELECT
            c.cluster_id,
            c.name,
            c.best_tweet_id,
            c.is_displayed,
            c.cluster_run_id,
            c.updated_timestamp,
            count(t.id) as tweets_count
        FROM tweets_clusters c join tweets_all t on c.cluster_id = t.cluster_id
            WHERE cluster_run_id = (select max(cluster_run_id) from tweets_clusters)
            GROUP BY cluster_id, name, best_tweet_id, is_displayed, cluster_run_id, updated_timestamp
            ORDER BY count(t.id) desc
            LIMIT 1000';
$result = mysql_query($sql, $link);

if (!$result) {
    echo "DB Error, could not query the database\n";
    echo 'MySQL Error: ' . mysql_error();
    exit;
}
?>
<div class="page-header">
    <h1>All latest clusters</h1>
</div>
<div class="container-fluid">
<?
while ($row = mysql_fetch_assoc($result)) {
    ?>

    <div class="row-fluid">
    <div class="col-md-1"><?= $row['cluster_id'] ?></div>
    <div class="col-md-1"><?= $row['tweets_count'] ?></div>
    <div class="col-md-1"><?= $row['tweets_count'] ?></div>
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
