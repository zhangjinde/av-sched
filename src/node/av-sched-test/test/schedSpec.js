var Promise = require("bluebird");
var express = require("express");
var rp = require("request-promise");
var assert = require("chai").assert;

function startListener(state, id, secret) {
    return function () {
    return new Promise(function (resolve, reject) {
        var app = express();

        app.post("/test/" + id, function (req, res) {
            //console.log("Received request from av-sched");
            res.status(200).json({});
            var header = req.headers["x-sched-secret"];
            assert.equal(header, secret, "Missing x-sched-secret header");
            state.count = state.count + 1;
        });

        state.server = app.listen(3000, function () {
            resolve();
        });
    });
    };
}

function scheduleJob(id, interval, secret) {
    return function () {
        //console.log("Scheduling job");
        return rp({
            uri : "http://localhost:8086/sched/api/job",
            method : "POST",
            headers : {
                "X-sched-secret" : secret
            },
            body : JSON.stringify({
                config : {
                    id : id,
                    url : "http://localhost:3000/test/" + id,
                    timeout : 60000
                },
                scheduling : {
                    type : "cron",
                    value : "0/"+ interval + " 0/1 * 1/1 * ? *"
                }
            })
        });
    };

}

function waitFor(seconds) {
    return function () {
        //console.log("Waiting for " + seconds + " seconds");
        return new Promise(function (resolve, reject) {
            setTimeout(function () {
                resolve();
            }, seconds * 1000);
        });
    };
}

function checkCalls(state, count) {
    return function () {
        //console.log("Checking state");
        return new Promise(function (resolve, reject) {
            assert.equal(state.count, count, "Unexpected number of server call");
            resolve();
        });
    };
}

function ackJob(id, secret) {
    return function () {
        //console.log("Acking job");
        return rp({
            uri : "http://localhost:8086/sched/api/job/ack",
            method : "POST",
            headers : {
                "x-sched-secret" : secret,
                "content-type" : "application/json"
            },
            body : JSON.stringify({
                id : id
            })
        });
    };
}

function stopServer(state) {
    return function () {
        //console.log("Ideally, end of the test");
        return true;
    };
    // state.server.end();
}

describe("av-sched", function () {

    it("can schedule cron jobs until ack arrives", function () {

        var jobId = "test-job-" + new Date().getTime();

        var secret = "secret";
        var state = {
            count : 0
        };
        return startListener(state,jobId,secret)()
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
            .then(stopServer(state));

    });

});
