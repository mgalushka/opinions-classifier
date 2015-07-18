<?php
header('Content-type: text/html; charset=utf-8');
include 'db.php';
include 'header.php';

$account_id = 1;
if (array_key_exists("id", $_GET)) {
    $account_id = intval($_GET['id']);
}

$link = connect();
?>
<div id="status">Saved</div>

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
<?
$max_run = intval(DB::queryFirstRow(
    sprintf("
            SELECT max(cluster_run_id) AS max_run
            FROM tweets_clusters
            WHERE account_id = %d
        ",
        $account_id
    )
)['max_run']);

$count_row = DB::queryFirstRow(
    sprintf("
        SELECT
             count(c.best_tweet_id) AS clusters_count
        FROM
             tweets_clusters c JOIN tweets_all t
             ON c.best_tweet_id = t.id
        WHERE
             c.cluster_run_id = %d AND
             c.is_displayed = 1 AND
             t.excluded = 0
        ",
        $max_run
    )
);
?>
<div class="page-header">
    <h3>Cleaned tweets [<?= $account_id; ?>]: <b><?= $count_row['clusters_count'] ?></b>
        <!--small>Subtext for header</small-->
    </h3>
</div>

<div class="container-fluid">
    <?
    $sql = sprintf('
        SELECT
            t.id,
            t.content_json,
            t.tweet_cleaned,
            t.features,
            t.label,
            DATE_FORMAT(t.created_timestamp, "%%Y-%%m-%%d %%H:%%i") AS created_timestamp,
            count(r.tweet_id) AS tweets_in_cluster
        FROM
            tweets_clusters c JOIN tweets_all t
              ON c.best_tweet_id = t.id AND
                c.account_id = t.account_id
            JOIN clusters_runs r
              ON c.cluster_id = r.cluster_id
            LEFT JOIN tweets_scheduled s
              ON t.id = s.id
        WHERE
            t.account_id = %d AND
            c.cluster_run_id = %d AND
            c.is_displayed = 1 AND
            t.excluded = 0 AND
            s.id IS NULL
        GROUP BY
            1, 2, 3, 4, 5, 6
        ORDER BY
            t.label DESC,
            count(r.tweet_id) DESC
        ',
        $account_id,
        $max_run
    );
    $result = mysqli_query($link, $sql);
    while ($row = mysqli_fetch_assoc($result)) {
        $id = $row['id'];
        $label = $row['label'];
        $label_class = 'label';
        if ($label === 'pos') {
            $label_class .= ' label-success';
        }
        if ($label === 'neg') {
            $label_class .= ' label-danger';
        }
        $tweet_json = preg_replace('/[\x00-\x1F\x80-\xFF]/', '', $row['content_json']);
        $tweet_json_read = preg_replace('/,/', ', ', $tweet_json);
        $features = preg_replace('/,/', ', ', $row['features']);
        $tweet_author = htmlentities(json_decode($tweet_json, true)['user']['name']);
        $tweet_login = htmlentities(json_decode($tweet_json, true)['user']['screen_name']);
        $urls = json_decode($tweet_json, true)['entities']['urls'];
        $tweet_link = '';
        if ($urls) {
            $tweet_link = $urls[0]['expanded_url'];
        }
        ?>
        <div id="row_<?= $id ?>" class="row">
            <div class="col-md-9 col-xs-9">
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <?= $tweet_login ?> (<?= $tweet_author ?>)
                        (<?= $id ?>) (<?= $row['tweets_in_cluster'] ?>)
                        <span class="<?= $label_class ?>"><?= $label ?></span>
                    </div>
                    <div id="text_<?= $id ?>" class="panel-body"
                         style="word-wrap:break-word"><?= $row['tweet_cleaned'] ?>
                        <a href="<?= $tweet_link ?>" target="_blank" style="word-wrap:break-word"><?= $tweet_link ?></a>
                    </div>
                    <div class="panel-footer">
                        <?= $row['created_timestamp'] ?>
                        <button class="btn btn-primary" type="button" data-toggle="collapse"
                                data-target="#article_<?= $id ?>" aria-expanded="false" aria-controls="collapseExample">
                            Content
                        </button>
                        <div class="collapse" id="article_<?= $id ?>" data-id="<?= $id ?>"
                             data-url="<?= $tweet_link ?>">
                            <div class="well">
                                <img src="images/ajax-loader.gif"/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-md-3 col-xs-3">
                <div class="btn-group-vertical" role="group" aria-label="...">
                    <button data-id="<?= $id ?>" data-action="retweet" type="button" class="btn btn-lg retweet">
                        <span class="glyphicon glyphicon-send" aria-hidden="true"></span>&nbsp;RT
                    </button>
                    <button data-id="<?= $id ?>" data-action="update" data-toggle="modal"
                            data-target="#edit-tweet-modal"
                            type="button"
                            class="btn btn-lg btn-success">
                        <span class="glyphicon glyphicon glyphicon-ok" aria-hidden="true"></span>&nbsp;TW
                    </button>
                    <button data-id="<?= $id ?>" data-action="interested" type="button"
                            class="btn btn-lg btn-info interested-not-now">
                        <span class="glyphicon glyphicon-thumbs-up" aria-hidden="true"></span>&nbsp;NN
                    </button>
                    <button data-id="<?= $id ?>" data-action="delete" type="button"
                            class="btn btn-lg btn-danger delete">
                        <span class="glyphicon glyphicon-thumbs-down" aria-hidden="true"></span>&nbsp;DEL
                    </button>
                </div>
            </div>
        </div>
    <?
    }
    ?>
</div>
<!-- Modal -->
<div class="modal modal-sm" id="edit-tweet-modal" tabindex="-1" role="dialog" aria-labelledby="edit-tweet-modal-label"
     aria-hidden="true">
    <div class="modal-dialog modal-sm">
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

mysqli_free_result($result);
mysqli_close($link);

include 'footer.php';
?>
