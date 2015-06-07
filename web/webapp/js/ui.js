var NavigationContainer = React.createClass({
    render: function() {

        return <p>{this.props.text}</p>;
    }
});

React.render(
    <NavigationContainer text="" />,
    document.getElementById('footer')
);
