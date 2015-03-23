import EventEmitter from "events";
import Immutable from "immutable";

import AppDispatcher from "../dispatcher/appDispatcher";
import {ActionTypes} from "../constants/appConstants";

const CHANGE_EVENT = "change";

var _jobs = Immutable.Map();

var _emitter = new EventEmitter();

var JobStore = {

    emitChange : function () {
        _emitter.emit(CHANGE_EVENT);
    },

    addChangeListener : function (callback) {
        _emitter.on(CHANGE_EVENT, callback);
    },

    removeChangeListener : function(callback) {
        _emitter.removeListener(CHANGE_EVENT, callback);
    },

    getAll : function() {
        return Immutable.List(_jobs.values());
    }

};

JobStore.dispatchTocken = AppDispatcher.register(function (action) {

    switch (action.type) {
    case ActionTypes.RECEIVE_JOBS:
        _jobs = action.jobs.reduce(function (accu, job) {
            return accu.set(job.config.id, job);
        }, Immutable.Map());
        JobStore.emitChange();
        break;

    case ActionTypes.JOB_DELETED:
        _jobs = _jobs.deleteIn(action.jobId);
        JobStore.emitChange();
        break;

    case ActionTypes.JOB_UNLOCKED:
        // Simply updating client-side.
        // So there might be consistency issues, I suppose...
        // or should it ?
        _jobs = _jobs.updateIn([action.jobId], function (job) {
            job.lock = {
                locked : false,
                expiresAt : null
            };
            return job;
        });
        JobStore.emitChange();
        break;

    default:
    }

});


export default JobStore;
