<?php
include 'db.php';
$sys = strtoupper(PHP_OS);
$JPGRAPH_ROOT = "/home/ec2-user/jpgraph-3.5.0b1/"
if(substr($sys,0,3) == "WIN"){
    $JPGRAPH_ROOT = "D:/dev/php/jpgraph-3.5.0b1.tar/jpgraph-3.5.0b1/"
}
require_once($JPGRAPH_ROOT . 'src/jpgraph.php');
require_once($JPGRAPH_ROOT . 'src/jpgraph_line.php');

$sql = '
    select
    unix_timestamp(date(created_timestamp)) as dt, count(1) as cnt
    from tweets_all
    where created_timestamp > DATE_SUB(now(), INTERVAL 30 DAY)
    group by date(created_timestamp)
    order by date(created_timestamp)
';
$link = connect();
$result = mysql_query($sql, $link);
$xdata = [];
$ydata = [];

// Some userdefined human readable version of the timestamp
function formatDate(&$aVal) {
    $aVal = date('Y-m-d', $aVal);
}

while ($rows = mysql_fetch_array($result)) {
    array_push($xdata, $rows['dt']);
    array_push($ydata, $rows['cnt']);
}

array_walk($xdata,'formatDate');



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

// Display the graph
$graph->Stroke()

?>
