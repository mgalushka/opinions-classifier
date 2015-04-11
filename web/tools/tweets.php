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

<div class="container-fluid">
    <?
    $sql = sprintf('
        select
            t.id,
            t.content_json,
            t.tweet_cleaned,
            t.features,
            DATE_FORMAT(t.created_timestamp, "%%Y-%%m-%%d %%H:%%i") as created_timestamp
        from
            tweets_clusters c join tweets_all t
            ON c.best_tweet_id = t.id
        where
            cluster_run_id = (select max(cluster_run_id) from tweets_clusters)
    ');
    $result = mysql_query($sql, $link);
    while ($row = mysql_fetch_assoc($result)) {
        $id = $row['id'];
        $tweet_json = preg_replace('/[\x00-\x1F\x80-\xFF]/', '', $row['content_json']);
        $tweet_json_read = preg_replace('/,/', ', ', $tweet_json);
        $features = preg_replace('/,/', ', ', $row['features']);
        $tweet_author = htmlentities(json_decode($row['content_json'], true)['author']);
        $tweet_author = htmlentities(json_decode($row['content_json'], true)['author']);
        ?>
        <div id="row_<?= $id ?>" class="row">
            <div class="col-md-9 col-xs-9">
                <div class="panel panel-default">
                    <div class="panel-heading"><?= $tweet_author ?></div>
                    <div id="text_<?= $id ?>" class="panel-body"><?= $row['tweet_cleaned'] ?></div>
                    <div class="panel-footer"><?= $row['created_timestamp'] ?></div>
                </div>
            </div>
            <div class="col-md-3 col-xs-3">
                <div class="btn-group-vertical" role="group" aria-label="...">
                    <button data-id="<?= $id ?>" data-action="retweet" type="button" class="btn btn-lg retweet">RT</button>
                    <button data-id="<?= $id ?>" data-action="update" data-toggle="modal" data-target="#edit-tweet-modal"
                            type="button"
                            class="btn btn-lg">TW</button>
                    <button data-id="<?= $id ?>" data-action="delete" type="button" class="btn btn-lg btn-danger">DEL</button>
                </div>
            </div>
        </div>
    <?
    }
    ?>
</div>

<?php

mysql_free_result($result);
mysql_close($link);

include 'footer.php';
?>
