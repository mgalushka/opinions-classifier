var tt = tt || {};

tt.retrieval = function () {

    var API = "http://warua.org:8080/retrieve?text=";
    var TWITTER_EMBED = "http://warua.org:8092/easy/embed?";

    var retrieveContent = function (accountId, url, obj) {
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
                url: API + url,
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