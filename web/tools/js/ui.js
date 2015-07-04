var tt = tt || {};

tt.core = function () {

    var API = "http://warua.org:8092/easy";
    var QUICK_UPDATE_FADE_OUT = 4000;

    var showStatus = function (data, status) {
        console.log("Status: " + status);
        console.log("Status data: " + data);
        if (status === 'success'){
            $('#status').text('success').show().fadeOut(QUICK_UPDATE_FADE_OUT);
        }
        else{
            $('#status').text('failed').show().fadeOut(QUICK_UPDATE_FADE_OUT);
        }
    };

    var statusUpdate = function (tweetId, text, command, obj) {
        var request = $.ajax({
            url: API + "/" + command,
            method: 'POST',
            data: {
                tweetId: tweetId,
                text: text
            },
            dataType: "json"
        });
        request.done(showStatus);
    };

    return {
        statusUpdate: statusUpdate
    };
}();