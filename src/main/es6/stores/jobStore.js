var JobStore = {};

var _jobs = [];

JobStore.fetch = function () {

    _jobs.push({
        config : {
            id : 42,
            url : "http://prout.fr"
        },
        lock : {
            locked : false
        }
    });

};

JobStore.getAll = function () {
    // TODO(pht) an immutable version of jobs ?
    return _jobs;
};

export default JobStore;


