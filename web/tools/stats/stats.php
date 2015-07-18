<?php

include 'db.php';

$scale = 'DAY';
if (isset($_REQUEST['scale'])) {
    $scale = $_REQUEST['scale'];
}

$scale_mysql_map = array(
    "MINUTE" => "%Y-%m-%d %H:%i",
    "HOUR" => "%Y-%m-%d %H:00",
    "DAY" => "%Y-%m-%d",
    "MONTH" => "%Y-%m",
);

$scale_php_map = array(
    "MINUTE" => "Y-m-d H:i",
    "HOUR" => "Y-m-d H:00",
    "DAY" => "Y-m-d",
    "MONTH" => "Y-m",
);

# ignore errors for image generation scripts
error_reporting(0);
ini_set('display_errors', 'Off');
DEFINE('DEFAULT_GFORMAT', 'png');

date_default_timezone_set('Europe/London');

$sys = strtoupper(PHP_OS);
$JPGRAPH_ROOT = "/var/www/jpgraph-3.5.0b1/";
if (substr($sys, 0, 3) == "WIN") {
    $JPGRAPH_ROOT = "D:/dev/php/jpgraph-3.5.0b1.tar/jpgraph-3.5.0b1/";
}
require_once($JPGRAPH_ROOT . 'src/jpgraph.php');
require_once($JPGRAPH_ROOT . 'src/jpgraph_line.php');

$sql = sprintf('
    SELECT
      unix_timestamp(DATE_FORMAT(created_timestamp, \'%s\')) AS dt,
      count(id) AS cnt
    FROM tweets_all
    WHERE
      created_timestamp > DATE_SUB(now(), INTERVAL 120 %s)
    GROUP BY DATE_FORMAT(created_timestamp, \'%s\')
    ORDER BY DATE_FORMAT(created_timestamp, \'%s\')
    ',
    $scale_mysql_map[$scale],
    $scale,
    $scale_mysql_map[$scale],
    $scale_mysql_map[$scale]
);

$link = connect();
$result = mysqli_query($link, $sql);
$xdata = array();
$ydata = array();

// Some user defined human readable version of the timestamp
function formatDate(&$aVal)
{
    global $scale;
    global $scale_php_map;
    $aVal = date($scale_php_map[$scale], $aVal);
}

while ($rows = mysqli_fetch_array($result)) {
    array_push($xdata, $rows['dt']);
    array_push($ydata, $rows['cnt']);
}

array_walk($xdata, 'formatDate');


// Size of the overall graph
$width = 600;
$height = 600;

// Create the graph and set a scale.
// These two calls are always required
$graph = new Graph($width, $height);
$graph->SetScale('intlin');

// Create the linear plot
$lineplot = new LinePlot($ydata);

$graph->xaxis->SetTickLabels($xdata);
$graph->xaxis->SetLabelAngle(90);

// Add the plot to the graph
$graph->Add($lineplot);
$graph->img->SetImgFormat('png');

// Display the graph
$graph->Stroke();
