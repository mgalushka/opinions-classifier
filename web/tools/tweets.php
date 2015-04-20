<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';
include 'header.php';

$link = connect();
?>
<nav class="navbar navbar-default">
    <div class="container-fluid">
        <!-- Brand and toggle get grouped for better mobile display -->
        <div class="navbar-header">
            <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                    data-target="#bs-example-navbar-collapse-1">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="#">TweetMaster</a>
        </div>

        <!-- Collect the nav links, forms, and other content for toggling -->
        <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
            <ul class="nav navbar-nav">
                <li class="active"><a href="#">Suggested</a></li>
                <li><a href="#">Deleted</a></li>
                <li><a href="#">Settings</a></li>
            </ul>
        </div>
        <!-- /.navbar-collapse -->
    </div>
    <!-- /.container-fluid -->
</nav>

<div class="page-header">
    <h3>Example page header <small>Subtext for header</small></h3>
</div>

<div class="container-fluid">
    <?
    // ide here is to calculate average and then show tweets in order 
    // of how far they are from average cluster size
    $sql_average = sprintf('
        SELECT AVG(cnt) as avg_count
        FROM(
            SELECT
                count(distinct r.tweet_id) as cnt
            FROM
                tweets_clusters c JOIN tweets_all t
                ON c.best_tweet_id = t.id
                JOIN clusters_runs r
                ON c.cluster_id = r.cluster_id
            WHERE
                c.cluster_run_id = (SELECT max(cluster_run_id) FROM tweets_clusters) AND
                c.is_displayed = 1 AND
                t.excluded = 0
            GROUP BY 
                r.cluster_id
        ) t
    ');
    $average = 0;
    $result_average = mysqli_query($link, $sql_average);
    while ($row = mysqli_fetch_assoc($result_average)) {
        $average = $row['avg_count'];
    }

    $sql = sprintf('
        SELECT
            t.id,
            t.content_json,
            t.tweet_cleaned,
            t.features,
            DATE_FORMAT(t.created_timestamp, "%%Y-%%m-%%d %%H:%%i") AS created_timestamp,
            count(r.tweet_id) as tweets_in_cluster
        FROM
            tweets_clusters c JOIN tweets_all t
            ON c.best_tweet_id = t.id
            JOIN clusters_runs r
            ON c.cluster_id = r.cluster_id
        WHERE
            c.cluster_run_id = (SELECT max(cluster_run_id) FROM tweets_clusters) AND
            c.is_displayed = 1 AND
            t.excluded = 0
        GROUP BY
            1, 2, 3, 4, 5
        ORDER BY
            ABS(count(r.tweet_id) - %f) ASC
        ', 
        $average
    );
    $result = mysqli_query($link, $sql);
    while ($row = mysqli_fetch_assoc($result)) {
        $id = $row['id'];
        $tweet_json = preg_replace('/[\x00-\x1F\x80-\xFF]/', '', $row['content_json']);
        $tweet_json_read = preg_replace('/,/', ', ', $tweet_json);
        $features = preg_replace('/,/', ', ', $row['features']);
        $tweet_author = htmlentities(json_decode($tweet_json, true)['user']['name']);
        $tweet_login = htmlentities(json_decode($tweet_json, true)['user']['screen_name']);
        $tweet_link = json_decode($tweet_json, true)['entities']['urls'][0]['expanded_url'];
        ?>
        <div id="row_<?= $id ?>" class="row">
            <div class="col-md-9 col-xs-9">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <?= $tweet_login ?> (<?= $tweet_author ?>)
                        (<?= $id ?>) (<?= $row['tweets_in_cluster'] ?>)
                    </div>
                    <div id="text_<?= $id ?>" class="panel-body"><?= $row['tweet_cleaned'] ?>
                        &nbsp;<a href="<?= $tweet_link ?>" target="_blank"><?= $tweet_link ?></a></div>
                    <div class="panel-footer"><?= $row['created_timestamp'] ?></div>
                </div>
            </div>
            <div class="col-md-3 col-xs-3">
                <div class="btn-group-vertical" role="group" aria-label="...">
                    <button data-id="<?= $id ?>" data-action="retweet" type="button" class="btn btn-lg retweet">RT
                    </button>
                    <button data-id="<?= $id ?>" data-action="update" data-toggle="modal"
                            data-target="#edit-tweet-modal"
                            type="button"
                            class="btn btn-lg">TW
                    </button>
                    <button data-id="<?= $id ?>" data-action="delete" type="button"
                            class="btn btn-lg btn-danger delete">DEL
                    </button>
                </div>
            </div>
        </div>
    <?
    }
    ?>
</div>
<!-- Modal -->
<div class="modal fade" id="edit-tweet-modal" tabindex="-1" role="dialog" aria-labelledby="edit-tweet-modal-label"
     aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span
                        aria-hidden="true">&times;</span></button>
                <h4 class="modal-title" id="myModalLabel">Edit tweet</h4>
            </div>
            <div class="modal-body">
                <div class="form-group">
                    <textarea id="original-tweet" class="form-control"></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
                <button id="schedule_tweet_id" data-tweet="" type="button" class="btn btn-primary" data-dismiss="modal">
                    Schedule
                </button>
            </div>
        </div>
    </div>
</div>

<?php

mysqli_free_result($result_average);
mysqli_free_result($result);
mysqli_close($link);

include 'footer.php';
?>
