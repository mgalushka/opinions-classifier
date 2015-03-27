<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';
include 'header.php';

$link = connect();

?>
<div class="page-header">
    <h1>All clusters</h1>
</div>
<div class="container-fluid">
<?
$sql = sprintf(
    'SELECT
        c.cluster_run_id,
        date(c.updated_timestamp) as dt,
        count(distinct c.cluster_id) as clusters_count,
        count(distinct t.id) as tweets_count
    FROM tweets_clusters c join tweets_all t on c.cluster_id = t.cluster_id
    GROUP BY
        c.cluster_run_id,
        date(c.updated_timestamp)
    ORDER BY
        c.cluster_run_id DESC
    LIMIT 25'
);
$result = mysql_query($sql, $link);
while ($row = mysql_fetch_assoc($result)) {
    ?>
    <table class="table table-hover">
        <tr>
            <td>
                <a href="clusters.php?run_id=<?= $row['cluster_run_id'] ?>">
                    <?= $row['cluster_run_id'] ?>
                </a>
            </td>
            <td><?= $row['dt'] ?></td>
            <td class="col-md-2"><?= $row['clusters_count'] ?></td>
            <td class="col-md-2"><?= $row['tweets_count'] ?></td>
        </tr>
    </table>
    <?
}
?>
</div>
<?

// getting cluster_id to display information for current cluster
$cluster_run_id = $_REQUEST['run_id'];
if (empty($cluster_run_id)) {
    $cluster_run_id = 82;
    $sql = sprintf(
        'SELECT
            max(cluster_run_id) as max_run_id
         FROM tweets_clusters',
         $cluster_run_id
    );
    $result = mysql_query($sql, $link);
    while ($row = mysql_fetch_assoc($result)) {
        $cluster_run_id = $row['max_run_id'];
    }
}

$sql = sprintf(
    'SELECT
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
    'SELECT
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
    'SELECT
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
    <h1>All latest clusters
        <small>(cluster_run_id=<?= $cluster_run_id ?>, total <?= $total_clusters ?>)</small>
    </h1>
</div>
<div class="container-fluid">
    <?php
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
<?php

mysql_free_result($result);
mysql_close($link);

include 'footer.php';
?>
