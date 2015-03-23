var Promise = require("bluebird");
var express = require("express");
var rp = require("request-promise");
var assert = require("chai").assert;
var _ = require("lodash");

var sched = {

    startListener: function(state, id, secret) {
        return function() {
            return new Promise(function(resolve, reject) {
                var app = express();

                app.post("/test/" + id, function(req, res) {
                    console.log("Received post requist on", req.path);
                    //console.log("Received request from av-sched");
                    res.status(200).json({}).end();
                    var header = req.headers["x-sched-secret"];
                    assert.equal(header, secret, "Missing x-sched-secret header");
                    state.count = state.count + 1;
                });

                state.app = app;
                console.log("Starting server listening on port", state.port);
                state.server = app.listen(state.port);
                state.server.on("listening", function() {
                    resolve();
                });
            });
        };
    },

    stopListener: function(state) {
        return function() {
            return new Promise(function(resolve, reject) {
                //            console.log("STOP Closing server", state.server);
                state.server.close();
                resolve();
            });
        };
    },

    scheduleJob: function(state, id, interval, secret) {
        return function() {
            console.log("Scheduling job on port", state.port, "with id", id);
            return rp({
                uri: "http://localhost:8086/sched/api/job-def",
                method: "POST",
                headers: {
                    "X-sched-secret": secret
                },
                body: JSON.stringify({
                    config: {
                        id: id,
                        url: "http://localhost:" + state.port + "/test/" + id,
                        timeout: 60000
                    },
                    scheduling: {
                        type: "cron",
                        value: "0/" + interval + " 0/1 * 1/1 * ? *"
                    }
                })
            });
        };

    },

    unscheduleJob : function (state, id, secret) {

        return function () {
            console.log("Unscheduling job on port", state.port);
            return rp({
                uri : "http://localhost:8086/sched/api/job-def",
                method : "DELETE",
                headers : {
                    "X-sched-secret" : secret
                },
                body : JSON.stringify({
                    id : id
                })
            });
        };

    },

    waitFor: function(seconds) {
        return function() {
            //console.log("Waiting for " + seconds + " seconds");
            return new Promise(function(resolve, reject) {
                setTimeout(function() {
                    resolve();
                }, seconds * 1000);
            });
        };
    },
    checkCalls: function(state, count, message) {
        return function() {
            //console.log("Checking state");
            return new Promise(function(resolve, reject) {
                assert.equal(state.count, count, "Unexpected number of server call (try " + message + ")");
                resolve();
            });
        };
    },

    ackJob: function(id, secret) {
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
    },

    getJobs: function() {
        return rp({
            uri: "http://localhost:8086/sched/api/job",
            method: "GET",
            headers: {
                "content-type": "application/json"
            },
            json: true
        });
    },

    findJob: function(jobId, jobs) {
        return _.find(jobs, function(job) {
            return job.config.id === jobId;
        });
    },

    checkHasNoJobState: function(jobId) {
        return function() {
            return sched.getJobs().then(function(jobs) {
                var job = sched.findJob(jobId, jobs);
                assert.isUndefined(job);
            });
        };
    },

    checkHasJobState: function(jobId, test) {
        return function() {
            return sched.getJobs().then(function(jobs) {
                var job = sched.findJob(jobId, jobs);
                assert.ok(job, "A job with the id " + jobId + " should have been found");
                if (test && test.lock) {
                    assert.equal(test.lock.locked, job.lock.locked, "Expected lock state :" + test.lock.locked);
                }
                if (test && test.scheduling) {
                    assert.deepEqual(test.scheduling, job.scheduling, "Expected job scheduling : " + test.scheduling);
                }
            });
        };
    }

};

module.exports = sched;
