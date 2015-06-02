var subscribe = subscribe || {};

subscribe.core = function () {

    var API = "http://lightbot.co/subscribe.php";

    var showSuccess = function () {
        // TODO
        console.log("Success");
    };

    var subscribe = function (email) {
        $.ajax({
            url: API,
            method: 'POST',
            data: {
                email: email
            },
            success: showSuccess(),
            error: showSuccess(),
            dataType: "json"
        });
    };

    return {
        subscribe: subscribe
    };
}();

$(document).ready(function () {
    $('#subscribe-btn').on('click', function (event) {
        var email = $('#email-input').val().trim();
        subscribe.core.subscribe(email);
    });
});