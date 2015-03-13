var Promise = require("bluebird");
var express = require("express");
var rp = require("request-promise");
var assert = require("chai").assert;
var _ = require("lodash");

function startListener(state, id, secret) {
    return function() {
        return new Promise(function(resolve, reject) {
            var app = express();

            app.post("/test/" + id, function(req, res) {
                //console.log("Received request from av-sched");
                res.status(200).json({}).end();
                req.connection.destroy();
                var header = req.headers["x-sched-secret"];
                assert.equal(header, secret, "Missing x-sched-secret header");
                state.count = state.count + 1;
            });

            state.app = app;
            state.server = app.listen(3000, function() {
                resolve();
            });
        });
    };
}

function stopListener(state) {
    return function() {
        return new Promise(function(resolve, reject) {
            console.log("STOP Closing server", state.server);
            state.server.close(function() {
                console.log("STOP Closed server");
                resolve();
            });
        });
    };
}

function scheduleJob(id, interval, secret) {
    return function() {
        //console.log("Scheduling job");
        return rp({
            uri: "http://localhost:8086/sched/api/job-def",
            method: "POST",
            headers: {
                "X-sched-secret": secret
            },
            body: JSON.stringify({
                config: {
                    id: id,
                    url: "http://localhost:3000/test/" + id,
                    timeout: 60000
                },
                scheduling: {
                    type: "cron",
                    value: "0/" + interval + " 0/1 * 1/1 * ? *"
                }
            })
        });
    };

}

function waitFor(seconds) {
    return function() {
        //console.log("Waiting for " + seconds + " seconds");
        return new Promise(function(resolve, reject) {
            setTimeout(function() {
                resolve();
            }, seconds * 1000);
        });
    };
}

function checkCalls(state, count) {
    return function() {
        //console.log("Checking state");
        return new Promise(function(resolve, reject) {
            assert.equal(state.count, count, "Unexpected number of server call");
            resolve();
        });
    };
}

function ackJob(id, secret) {
    return function() {
        //console.log("Acking job");
        return rp({
            uri: "http://localhost:8086/sched/api/job-action/ack",
            method: "POST",
            headers: {
                "x-sched-secret": secret,
                "content-type": "application/json"
            },
            body: JSON.stringify({
                id: id
            })
        });
    };
}

function getJobs() {
    return rp({
        uri: "http://localhost:8086/sched/api/job",
        method: "GET",
        headers: {
            "content-type": "application/json"
        },
        json: true
    });
}

function findJob(jobId, jobs) {
    return _.find(jobs, function(job) {
        return job.config.id === jobId;
    });
}

function checkHasNoJobState(jobId) {
    return function() {
        return getJobs().then(function(jobs) {
            var job = findJob(jobId, jobs);
            assert.isUndefined(job);
        });
    };
}

function checkHasJobState(jobId, test) {
    return function() {
        return getJobs().then(function(jobs) {
            var job = findJob(jobId, jobs);
            assert.ok(job, "A job with the right id should have been found");
            if (test) {
                assert.equal(test.locked, job.lock.locked, "Expected lock state :" + test.locked);
            }
        });
    };
}

describe("av-sched", function() {

    var jobId = null;
    var secret = "secret";
    var state = null;

    beforeEach(function() {
        jobId = "test-job-" + new Date().getTime();

        return new Promise(function (resolve, reject) {
            if (state && state.server) {
                console.log("BEF Closing server");
                console.log(state.server.getConnections());
                state.server.close(function () {
                    console.log("BEF Closed server");

                    state = {
                        count: 0
                    };


                    resolve();
                });
            } else {

                state = {
                    count: 0
                };

                resolve();
            }
        });

    });

    it("can schedule cron jobs until ack arrives", function() {

        console.log("TEST1");

        return startListener(state, jobId, secret)()
            .then(checkCalls(state, 0))
            .then(scheduleJob(jobId, 2, secret))
            .then(waitFor(2))
            .then(checkCalls(state, 1))
            .then(waitFor(2))
            .then(checkCalls(state, 1))
            .then(waitFor(2))
            .then(checkCalls(state, 1))
            .then(ackJob(jobId, secret))
            .then(waitFor(2))
            .then(checkCalls(state, 2))
            .then(stopListener(state));

    });

    xit("can list jobs", function() {

        console.log("TEST2");

        return startListener(state, jobId, secret)().
        then(checkHasNoJobState(jobId))
            .then(scheduleJob(jobId, 2, secret))
            .then(checkHasJobState(jobId, {
                locked: false
            }))
            .then(waitFor(2))
            .then(checkHasJobState(jobId, {
                locked: true
            }))
            .
        catch (stopListener(state));

    });

});
