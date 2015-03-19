var sched = require("./sched.js");

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

    it("can remove a scheduled job", function () {
        console.log("Testing removing a job...");

        return sched.startListener(state, jobId, secret)()
            .then(sched.checkCalls(state, 0, "0"))
            .then(sched.scheduleJob(state, jobId, 5, secret))
            .then(sched.unscheduleJob(state, jobId, secret))
            .then(sched.waitFor(5))
            .then(sched.checkCalls(state, 0, "1"))
            .then(sched.stopListener(state));
    });

    it("can schedule cron jobs until ack arrives", function() {

        console.log("Testing job ack...");

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

    it("can list jobs", function() {

        console.log("Testing list of jobs...");

        return sched.startListener(state, jobId, secret)().
        then(sched.checkHasNoJobState(jobId))
            .then(sched.scheduleJob(state, jobId, 2, secret))
            .then(sched.checkHasJobState(jobId, {
                locked: false
            }))
            .then(sched.waitFor(2))
            .then(sched.checkHasJobState(jobId, {
                locked: true
            }))
            .
        catch (sched.stopListener(state));

    });

});
