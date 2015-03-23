jest.dontMock("../jobStore");
jest.dontMock('object-assign');

describe("JobStore", function () {

    var AppDispatcher, JobStore;
    var callback;

    beforeEach(function () {
        AppDispatcher = require("../../dispatcher/appDispatcher");
        JobStore = require("../jobStore");
        var calls = AppDispatcher.register.mock.calls;
        expect(calls.length).toBe(1);
        expect(calls[0].length).toBe(1);
        callback = calls[0][0];
    });

    it("register a callback", function () {
        expect(callback).not.toBe(null);
    });

    /* This should probably work in theory, but doing all the ES6 magic is just
too much for jest... :()
    it("removes deleted job", function () {

        expect(JobStore.getAll().count()).toBe(0);

        callback({
            type : "RECEIVE_JOBS",
            jobs : [{
                config : {
                    id : 42
                }
            }]
        });

        expect(JobStore.getAll().count()).toBe(1);

        callback({
            type : "JOB_DELETED",
            jobId : "42"
        });

        expect(JobStore.getAll().count()).toBe(1);

    });
     */

});
