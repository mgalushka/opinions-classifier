var tt = tt || {};

tt.retrieval = function () {

    var API = "article.php?tweet_id=";

    // Need corresponding apache configuration to support this - see README
    var TWITTER_EMBED = "/easy/embed?";

    var retrieveContent = function (accountId, url, tweet_id, obj) {
        // for twitter links - just use service to get embedded representation
        if (url.indexOf("twitter.com") > 0) {
            var tweetId = url.substring(url.lastIndexOf("/") + 1, url.length);
            console.log("Embedding tweet: " + tweetId);
            $.ajax({
                url: TWITTER_EMBED + 'tweetId=' + tweetId + '&id=' + accountId + '&url=' + url,
                method: 'GET',
                success: function (data) {
                    obj.html(data);
                },
                dataType: "html"
            });
        } else {
            $.ajax({
                url: API + tweet_id,
                method: 'GET',
                success: function (data) {
                    obj.html(data);
                },
                dataType: "html"
            });
        }
    };

    return {
        retrieveContent: retrieveContent
    };
}();