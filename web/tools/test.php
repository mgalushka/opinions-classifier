<?php

$json  = '{"id":578329764898979841,"text":"RT @JPexsquaddie: Canada\u0027s Daily Helping Raise Funds for Ukraine\u0027s Extreme-Right Militias!!!\n http://t.co/BvLIoWA4rM\nSHAME ON U CANADA htt�","user":{"screen_name":"Novorossiyan","name":"Novorossiya Lives"},"retweeted":false,"entities":{"urls":[{"expanded_url":"http://www.liveleak.com/view?i\u003d27b_1424698763#gp7e3kuPtGGDuVlp.99"}],"media":[{"media_url":"http://pbs.twimg.com/media/B-iZ33gIAAARYZv.jpg"}]},"favorite_count":0,"retweet_count":0}';
$html_tweet = htmlentities(json_decode($json, true)['text']);
echo $json;
echo '<br/>';
echo json_decode($json, true)['text'];
echo '<br/>';
echo htmlentities(json_decode($json, true)['text']);
?>