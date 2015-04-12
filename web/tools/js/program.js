$(document).ready(function () {

    var lastEditedText = '';

    $('.retweet').on('click', function () {
        var tweetId = $(this).attr('data-id');
        var action = $(this).attr('data-action');
        tt.core.statusUpdate(tweetId, '', action, $(this));
    });

    $('.delete').on('click', function () {
        var tweetId = $(this).attr('data-id');
        console.log("Deleting tweet: " + tweetId);
        tt.core.statusUpdate(tweetId, '', 'delete', $(this));
        $('#row_' + tweetId).addClass('hide');
    });

    $('#edit-tweet-modal').on('show.bs.modal', function (event) {
        var button = $(event.relatedTarget); // Button that triggered the modal
        var tweetId = button.attr('data-id');
        var originalTweet = $('#text_' + tweetId).text().replace(/ +/g, " ").trim();
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

        tt.core.statusUpdate(tweetId, editedText, 'update', $(this));
    });


});