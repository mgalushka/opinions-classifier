var CandidateControls = React.createClass({
    render: function () {
        return (
            <div class="col-md-3 col-xs-3">
                <div class="btn-group-vertical" role="group" aria-label="...">
                    <button data-id="123" data-action="retweet" type="button" class="btn btn-lg retweet">RT</button>
                    <button data-id="123" data-action="update" data-toggle="modal" data-target="#edit-tweet-modal"
                    type="button"
                    class="btn btn-lg">TW
                    </button>
                    <button data-id="123" data-action="delete" type="button" class="btn btn-lg btn-danger">DEL</button>
                </div>
            </div>
        );
    }
});

var CandidateItem = React.createClass({
    getInitialState: function () {
        return {
            hidden: false
        };
    },
    render: function () {
        var classes = 'row';
        if (this.props.hidden) {
            classes += ' hide';
        }
        return (
            <div class={classes}>
                <div class="col-md-9 col-xs-9">
                    <div class="panel panel-default">
                        <div class="panel-heading">{this.props.author}</div>
                        <div id="text_123" class="panel-body">{this.props.text}</div>
                        <div class="panel-footer">{this.props.timestamp}</div>
                    </div>
                </div>
                <CandidateControls />
            </div>
        );
    }
});

React.render(
    <CandidateItem
    author="@maria_engstrom"
    text="10 more questions Russian military pose to Ukraine, US &amp; @EliotHiggins over MH17 crash:"
    date="2015-04-11 16:01"/>,
    document.getElementById('candidates')
);
