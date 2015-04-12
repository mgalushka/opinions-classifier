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
    $sql = sprintf('
    SELECT
        c.cluster_run_id,
        DATE_FORMAT(c.updated_timestamp, "%%Y-%%m-%%d %%H:%%i") AS dt,
        count(DISTINCT c.cluster_id) AS clusters_count,
        count(DISTINCT r.tweet_id) AS tweets_count
    FROM tweets_clusters c JOIN clusters_runs r ON c.cluster_id = r.cluster_id
    GROUP BY
        c.cluster_run_id,
        date(c.updated_timestamp)
    ORDER BY
        c.cluster_run_id DESC
    LIMIT 5'
    );
    $result = mysqli_query($link, $sql);
    while ($row = mysqli_fetch_assoc($result)) {
        ?>
        <div class="container-fluid">
            <div class="row-fluid">
                <div class="col-md-3">
                    <table class="table table-hover">
                        <tr>
                            <td>
                                <a href="clusters.php?run_id=<?= $row['cluster_run_id'] ?>">
                                    <?= $row['cluster_run_id'] ?>
                                </a>
                            </td>
                            <td><?= $row['dt'] ?></td>
                            <td><?= $row['clusters_count'] ?></td>
                            <td><?= $row['tweets_count'] ?></td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    <?
    }
    ?>
</div>
<?

// getting cluster_id to display information for current cluster
$cluster_run_id = isset($_REQUEST['run_id']) ? $_REQUEST['run_id'] : '';
if (empty($cluster_run_id)) {
    $cluster_run_id = 0;
    $sql = sprintf('
        SELECT
          max(run_id) AS max_run_id
        FROM clusters_runs',
        $cluster_run_id
    );
    $result = mysqli_query($link, $sql);
    while ($row = mysqli_fetch_assoc($result)) {
        $cluster_run_id = $row['max_run_id'];
    }
}

$sql = sprintf('
    SELECT
        count(t.id) AS total_tweets
    FROM clusters_runs r JOIN tweets_all t ON r.tweet_id = t.id
        WHERE r.run_id = %d',
    $cluster_run_id
);
$result = mysqli_query($link, $sql);
$total_tweets = 0;
while ($row = mysql_fetch_assoc($result)) {
    $total_tweets = $row['total_tweets'];
}

$sql = sprintf('
    SELECT
        count(1) AS total_clusters
    FROM clusters_runs r
    WHERE r.run_id = %d',
    $cluster_run_id
);
$result = mysqli_query($link, $sql);
$total_clusters = 0;
while ($row = mysql_fetch_assoc($result)) {
    $total_clusters = $row['total_clusters'];
}

$sql = sprintf('
    SELECT
        c.cluster_id,
        c.name,
        c.best_tweet_id,
        c.is_displayed,
        c.cluster_run_id,
        c.updated_timestamp,
        count(r.tweet_id) AS tweets_count
    FROM tweets_clusters c JOIN clusters_runs r
      ON c.cluster_id = r.cluster_id
    WHERE r.run_id = %d
    GROUP BY
      cluster_id,
      name,
      best_tweet_id,
      is_displayed,
      cluster_run_id,
      updated_timestamp
    ORDER BY
      count(r.tweet_id) DESC
    LIMIT 1000',
    $cluster_run_id
);
$result = mysqli_query($link, $sql);

$best_tweets = array();
$tweet_sql = sprintf('
    SELECT
        c.cluster_id,
        t.tweet_cleaned
    FROM tweets_clusters c LEFT OUTER JOIN tweets_all t
      ON c.best_tweet_id = t.id
    WHERE
       cluster_run_id = %d',
    $cluster_run_id
);
$tweet_result = mysqli_query($link, $tweet_sql);
while ($tweet_row = mysqli_fetch_assoc($tweet_result)) {
    $best_tweets[$tweet_row['cluster_id']] = $tweet_row['tweet_cleaned'];
}
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
        <div class="col-md-1"><?= round(100.0 * $row['tweets_count'] / $total_tweets, 2) ?>%</div>
        <div class="col-md-4">
            <a href="cluster.php?cluster_id=<?= $row['cluster_id'] ?>">
                <?= $row['name'] ?>
            </a>
        </div>
        <div class="col-md-5"><?= $best_tweets[$row['cluster_id']] ?>&nbsp;</div>
        </div><?php
    }
    ?>
</div>
<?php

mysqli_free_result($result);
mysqli_close($link);

include 'footer.php';
?>
