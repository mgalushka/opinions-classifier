<?php
include 'db.php';
require_once('D:/dev/php/jpgraph-3.5.0b1.tar/jpgraph-3.5.0b1/src/jpgraph.php');
require_once('D:/dev/php/jpgraph-3.5.0b1.tar/jpgraph-3.5.0b1/src/jpgraph_line.php');

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
