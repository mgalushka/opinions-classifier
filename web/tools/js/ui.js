var tt = tt || {};

tt.core = function () {

    var API = "http://localhost:8092/easy";

    var showSuccess = function (obj) {
        obj.addClass('btn-success');
    };

    var showError = function () {
        $('.error').removeClass('hide');
    };

    var statusUpdate = function (tweetId, text, command, obj) {
        $.ajax({
            url: API + "/" + command,
            method: 'POST',
            data: {
                tweetId: tweetId,
                text: text
            },
            success: showSuccess(obj),
            error: function (data) {
                console.log("Error during remote call to: " + API);
                showError();
            },
            dataType: "json"
        });
    };

    return {
        statusUpdate: statusUpdate
    };
}();