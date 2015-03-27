var sched = require("./sched.js");
var assert = require("chai").assert;

sched.LOG = false;

describe("av-sched", function() {

    var jobId = null;
    var secret = "secret";
    var state = null;
    var port = 0;

    beforeEach(function() {
        port = port + 10;
        state = {
            count: 0,
            port: 3000 + port
        };
        jobId = "test-job-" + new Date().getTime();
    });

    it("can schedule cron jobs until ack arrives", function() {

        sched.log("Testing job ack...");

        return sched.startListener(state, jobId, secret)()
            .then(sched.checkCalls(state, 0, "0"))
            .then(sched.scheduleJob(state, jobId, 2, secret))
            .then(sched.waitFor(4))
            .then(sched.checkCalls(state, 1, "1"))
            .then(sched.waitFor(2))
            .then(sched.checkCalls(state, 1, "2"))
            .then(sched.waitFor(2))
            .then(sched.checkCalls(state, 1, "3"))
            .then(sched.ackJob(jobId, secret))
            .then(sched.waitFor(2))
            .then(sched.checkCalls(state, 2, "4"))
            .then(sched.stopListener(state));

    });

    it("can retrieve a single job", function () {
        return sched.scheduleJob(state, "test-query-single", 5, secret)()
            .then(function () {
                return sched.getJob("test-query-single");
            })
            .then(function (jobs) {
                assert.deepEqual([{
                    config : {
                        id : "test-query-single",
                        url : "http://localhost:" + state.port + "/test/test-query-single",
                        timeout : 60000
                    },
                    scheduling : {
                        type : "cron",
                        value : "0/5 0/1 * 1/1 * ? *"
                    },
                    lock : {
                        expired : false,
                        locked : false,
                        expiresAt : null
                    }
                }], jobs);
            });
    });

    it("can list jobs", function() {

        sched.log("Testing list of jobs...");

        return sched.startListener(state, jobId, secret)()
            .then(sched.checkHasNoJobState(jobId))
            .then(sched.scheduleJob(state, jobId, 2, secret))
            .then(sched.checkHasJobState(jobId, {
                scheduling : {
                    type : "cron",
                    value : "0/2 0/1 * 1/1 * ? *"
                },
                lock : {
                    locked: false
                }
            }))
            .then(sched.waitFor(3))
            .then(sched.checkHasJobState(jobId, {
                scheduling : {
                    type : "cron",
                    value : "0/2 0/1 * 1/1 * ? *"
                },
                lock : {
                    locked : true
                }
            }))
            .then(sched.stopListener(state));

    });

    it("can remove a scheduled job", function () {
        sched.log("Testing removing a job...");

        return sched.startListener(state, jobId, secret)()
            .then(sched.checkCalls(state, 0, "0"))
            .then(sched.scheduleJob(state, jobId, 5, secret))
            .then(sched.unscheduleJob(state, jobId, secret, {
                id : jobId,
                deleted : true
            }))
            .then(sched.waitFor(5))
            .then(sched.checkCalls(state, 0, "1"))
            .then(sched.stopListener(state));
    });

    it("silently removes non existing job", function () {
        return sched.unscheduleJob(state, jobId, secret, {
            id : jobId,
            deleted : false
        })();
    });

    it("can trigger job execution", function () {

        sched.log("Testing triggering job exec");

        return sched.startListener(state, jobId, secret)()
            .then(sched.checkCalls(state, 0, "0 - before job scheduling"))
            .then(sched.scheduleJob(state, jobId, 250000, secret))
            .then(sched.checkCalls(state, 0, "1 - after job scheduling"))
            .then(sched.triggerJob(jobId, secret, {
                triggered : true
            }))
            .then(sched.waitFor(2))
            .then(sched.checkCalls(state, 1, "2 - after job triggered once"))
            .then(sched.triggerJob(jobId, secret, {
                triggered : false
            }))
            .then(sched.waitFor(2))
            .then(sched.checkCalls(state, 1, "3 - after failed job triggering"))
            .then(sched.stopListener(state));
    });

    it("does not trigger unexisting job", function () {
        return sched.triggerJob("prout", secret)().then(function () {
            assert.fail(null, null, "Expected trigger call to fail");
        }, function (err) {
            assert.equal(err.response.statusCode, 500);
            assert.deepEqual(err.response.body, {
                error : "job.not.found",
                params : ["prout"]
            });
        });
    });

    it("does not ack unexisting job", function () {
        return sched.ackJob("prout", secret)().then(function () {
            assert.fail(null, null, "Expected ack call to fail");
        }, function (err) {
            assert.equal(err.response.statusCode, 500);
            assert.deepEqual(err.response.body, {
                error : "job.not.found",
                params : ["prout"]
            });
        });
    });


});
