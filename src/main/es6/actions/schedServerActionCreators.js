import AppDispatcher from "../dispatcher/appDispatcher";

import {ActionTypes} from "../constants/appConstants";

export default {

    jobReceived(job) {
        AppDispatcher.dispatch({
            type : ActionTypes.JOB_RECEIVED,
            job : job
        });
    },

    jobsReceived(jobs) {
        AppDispatcher.dispatch({
            type : ActionTypes.JOBS_RECEIVED,
            jobs : jobs
        });
    },


    jobDeleted(jobId) {
        AppDispatcher.dispatch({
            type : ActionTypes.JOB_DELETED,
            jobId : jobId
        });
    },

    jobUnlocked(jobId) {
        AppDispatcher.dispatch({
            type : ActionTypes.JOB_UNLOCKED,
            jobId : jobId
        });
    },

    jobTriggered(jobId) {
        AppDispatcher.dispatch({
            type : ActionTypes.JOB_TRIGGERED,
            jobId : jobId
        });
    }

};
