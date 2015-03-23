import React from "react";
import ReactB from "react-bootstrap";

import JobStore from "../stores/jobStore";

import JobsTable from "../views/jobsTable";

function getStateFromStore() {
    return {
        jobs : JobStore.getAll()
    };
}

class JobsPage extends React.Component {

    constructor() {
        this.state = getStateFromStore();
    }

    componentDidMount() {
        JobStore.addChangeListener(this._onChange.bind(this));
    }

    componentDidUnmount() {
        JobStore.removeChangeListener(this._onChange.bind(this));
    }

    render() {

        let navBar = React.createElement(ReactB.Navbar, {
            brand : "Av-Sched",
            children : []
        });

        let table = React.createElement(JobsTable, {
            jobs : this.state.jobs
        });

        let page = React.DOM.div({
            className : "container",
            children : [
                React.DOM.h2({
                    children : ["Scheduled jobs"]
                }),
                table]
        });

        return React.DOM.div({
            children : [navBar, page]
        });
    }

    _onChange() {
        this.setState(getStateFromStore());
    }
}

export default JobsPage;
