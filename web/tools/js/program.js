function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

$(document).ready(function () {

    var ACCOUNT_ID = getParameterByName('id');
    console.log("Account id: " + ACCOUNT_ID);

    var lastEditedText = '';

    $('.retweet').on('click', function () {
        var tweetId = $(this).attr('data-id');
        var action = $(this).attr('data-action');
        tt.core.statusUpdate(tweetId, '', action, ACCOUNT_ID, $(this));
        $('#row_' + tweetId).addClass('hide');
    });

    $('.delete').on('click', function () {
        var tweetId = $(this).attr('data-id');
        var action = $(this).attr('data-action');
        console.log("Deleting tweet: " + tweetId);
        tt.core.statusUpdate(tweetId, '', action, ACCOUNT_ID, $(this));
        $('#row_' + tweetId).addClass('hide');
    });

    $('.duplicate').on('click', function () {
        var tweetId = $(this).attr('data-id');
        var action = $(this).attr('data-action');
        console.log("Marking tweet as duplicated: " + tweetId);
        tt.core.statusUpdate(tweetId, '', action, ACCOUNT_ID, $(this));
        $('#row_' + tweetId).addClass('hide');
    });

    $('.interested-not-now').on('click', function () {
        var tweetId = $(this).attr('data-id');
        var action = $(this).attr('data-action');
        console.log("Marking tweet as interested: " + tweetId);
        tt.core.statusUpdate(tweetId, '', 'interesting', ACCOUNT_ID, $(this));
        $('#row_' + tweetId).addClass('hide');
    });

    $('#edit-tweet-modal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget); // Button that triggered the modal
        var tweetId = button.attr('data-id');
        var originalTweet = $('#text_' + tweetId).text().replace(/\s+/g, " ").trim().toLowerCase();
        originalTweet = originalTweet[0].toUpperCase() + originalTweet.slice(1);
        console.log("Original tweet: " + originalTweet);

        var modal = $(this);
        modal.find('#original-tweet').val(originalTweet);
        console.log("Textarea: " + modal.find('#original-tweet').val());
        // var recipient = button.data('whatever'); // Extract info from data-* attributes
        // If necessary, you could initiate an AJAX request here (and then do the updating in a callback).
        // Update the modal's content. We'll use jQuery here, but you could use a data binding library or other methods instead.

        // modal.find('.modal-title').text('New message to ' + recipient);
        // modal.find('.modal-body input').val(recipient)
        $('#schedule_tweet_id').attr('data-tweet', tweetId);
    });

    $('#schedule_tweet_id').on('click', function (event) {
        var tweetId = $('#schedule_tweet_id').attr('data-tweet');
        var editedText = $('#original-tweet').val().trim();
        console.log("Going to save tweet: " + tweetId + ' ' + editedText);

        tt.core.statusUpdate(tweetId, editedText, 'update', ACCOUNT_ID, $(this));
        $('#row_' + tweetId).addClass('hide');
    });

    $('.collapse').on('shown.bs.collapse', function (event) {
        tweetId = $(this).attr('data-id');
        url = $(this).attr('data-url');
        if (url) {
            console.log("Retrieving content for " + url);
            tt.core.statusUpdate(tweetId, '', 'interesting', ACCOUNT_ID, $(this));
            tt.retrieval.retrieveContent(ACCOUNT_ID, url, $(this));
        }
    })


});