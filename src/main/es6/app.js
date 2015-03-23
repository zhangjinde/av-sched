import React from "react";

import JobsPage from "./components/jobsPage";

import SchedApi from "./api/schedApi";

SchedApi.fetch();

React.render(React.createElement(JobsPage), document.getElementsByTagName("body")[0]);

