import JobsPage from "./views/jobsPage";
import JobStore from "./stores/jobStore";

JobStore.fetch().then(function () {
    React.render(<JobsPage/>, document.getElementById("root"));
});
