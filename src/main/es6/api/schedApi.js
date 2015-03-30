// import rp from "request-promise";

import xhr from "xhr";
import SchedServerActionCreators from "../actions/schedServerActionCreators.js";

// TODO(pht) find how to clean that up
const SCHED_API_URL = "http://localhost:8086/sched/api";

// The 'request promise' module seems to not be compilable anymore with
// babel, so I use this small wrapper instead.
function rp(options) {
    return new Promise(function (resolve, reject) {
        xhr(options, function (err, resp, body) {
            if (err) {
                reject(err);
            } else {
                if (resp.statusCode !== 200) {
                    reject(resp);
                } else {
                    resolve(body);
                }
            }
        });
    });
}

var SchedApi = {

    fetch(jobId) {

        return rp({
            uri: SCHED_API_URL + "/job?jobId=" + jobId,
            method: "GET",
            json: true
        }).then(function(jobs) {
            if (jobs && jobs.length > 0) {
                SchedServerActionCreators.jobReceived(jobs[0]);
            }
        });
    },

    fetchAll() {
        return rp({
            uri: SCHED_API_URL + "/job",
            method: "GET",
            json: true
        }).then(function (jobs) {
            SchedServerActionCreators.jobsReceived(jobs);
        });
    },

    delete(jobId, secret) {

        return rp({
            uri : SCHED_API_URL + "/job-def",
            method : "DELETE",
            json : {
                id : jobId
            },
            headers : {
                "X-Sched-secret" : secret
            }
        }).then(function () {
            SchedServerActionCreators.jobDeleted(jobId);
        });

    },

    unlock(jobId, secret) {

        return rp({
            uri : SCHED_API_URL + "/job-action/ack",
            method : "POST",
            json : {
                id : jobId
            },
            headers : {
                "X-Sched-secret" : secret
            }
        }).then(function () {
            // Or should we refetch the job ?
            // Or should the API return the new version of the job ?
            SchedServerActionCreators.jobUnlocked(jobId);
        });

    },

    trigger(jobId, secret) {
        return rp({
            uri : SCHED_API_URL + "/job-action/trigger",
            method : "POST",
            json : {
                id : jobId
            },
            headers : {
                "X-Sched-secret" : secret
            }
        }).then(function () {
            // Not sure if this one is usefull
            SchedServerActionCreators.jobTriggered(jobId);
            // Or should it be the store's job ?
            SchedApi.fetch(jobId);
        });
    }

};

export default SchedApi;
