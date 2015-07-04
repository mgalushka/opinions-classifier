var tt = tt || {};

tt.core = function () {

    var API = "http://warua.org:8092/easy";

    var showStatus = function (data, status) {
        console.log("Status: " + status);
        console.log("Status data: " + data);
        if (status === 'success'){
            $('#status').text('success').show().fadeOut(2000);
        }
        else{
            $('#status').text('failed').show().fadeOut(2000);
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