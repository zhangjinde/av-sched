import AppDispatcher from "../dispatcher/appDispatcher";

import {ActionTypes} from "../constants/appConstants";

export default {

    receiveJobs(jobs) {
        AppDispatcher.dispatch({
            type : ActionTypes.RECEIVE_JOBS,
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
    }

};
