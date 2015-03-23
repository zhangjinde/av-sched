function fromNames(names) {
    return names.reduce(function (accu, name) {
        accu[name] = name;
        return accu;
    }, {});
}

export default {
    ActionTypes : fromNames(["RECEIVED_JOBS",
                             "DELETE_JOB",
                             "JOB_DELETED",
                             "UNLOCK_JOB",
                             "JOB_UNLOCKED"])
};
