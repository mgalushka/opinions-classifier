var subscribe = subscribe || {};

subscribe.core = function () {

    var API = "http://lightbot.co/subscribe.php";

    var showSuccess = function () {
        console.log("Success");
        alert("Thanks for registering!");
    };
    
    var showError = function () {
        console.log("Error");
        alert("Something went wrong. Please, try again later.");
    };

    var subscribe = function (email) {
        $.ajax({
            url: API,
            method: 'POST',
            data: {
                email: email
            },
            success: function(){
                showSuccess();
            },
            error: function(){
                showError();
            },
            dataType: "json"
        });
    };

    return {
        subscribe: subscribe
    };
}();

var email_regexp = /^([\w-]+(?:\.[\w-]+)*)@((?:[\w-]+\.)*\w[\w-]{0,66})\.([a-z]{2,6}(?:\.[a-z]{2})?)$/i;

$(document).ready(function () {
    $('#subscribe-btn').on('click', function (event) {
        var email = $('#email-input').val().trim();
        if(email_regexp.test(email)){
            subscribe.core.subscribe(email);
        } else {
            alert("Looks like email you have entered is invalid.\nPlease, try again.");
        }
    });
});