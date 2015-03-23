import AppDispatcher from "../dispatcher/appDispatcher";
import {ActionTypes} from "../constants/appConstants";
import SchedApi from "../api/schedApi";

export default {

    deleteJob(jobId, secret) {
        AppDispatcher.dispatch({
            type : ActionTypes.DELETE_JOB,
            jobId : jobId
        });
        SchedApi.delete(jobId, secret);
    },

    unlockJob(jobId, secret) {
        AppDispatcher.dispatch({
            type : ActionTypes.DELETE_JOB,
            jobId : jobId
        });
        SchedApi.unlock(jobId, secret);
    }

};
