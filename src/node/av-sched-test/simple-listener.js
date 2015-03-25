var express = require("express");
var app = express();

app.post("/", function (req, res) {
    if (req.headers['x-sched-secret'] === "secret") {
        console.log("Received POST from av-sched");
        res.status(200).json({
            id : "simple-job"
        });
    } else {
        console.log("Received POST with wrong secret");
        res.status(500).json({});
    }
});

app.listen(3000, function () {
    console.log("Example server running on port 3000");
});
