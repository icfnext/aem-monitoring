

let minute = 1000*60;
let hour = minute*60;
let day = hour*24;
let week = day*7;

var TIME_CONSTANTS = {};

TIME_CONSTANTS.INDICES = {
    MINUTE: "0",
    HOUR: "1",
    DAY: "2",
    WEEK: "3"
};

TIME_CONSTANTS.TIMES_IN_MS = {
    "0": minute,
    "1": hour,
    "2": day,
    "3": week,
};

export default TIME_CONSTANTS;