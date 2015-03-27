class HelloMessage extends React.Component {
    render() {
        return React.DOM.div({
            children : ["Hello, " + this.props.name + " ? "]
        });
    }
}

export default HelloMessage;
