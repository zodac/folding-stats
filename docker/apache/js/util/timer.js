const NUMBER_OF_UPDATES_PROPERTY_NAME = "numberOfUpdates";
const UPDATE_MINUTE = %UPDATE_MINUTE%;

function startTimer() {
    calculateNumberOfUpdates();
    updateTimer();
}

function calculateNumberOfUpdates() {
    var now = new Date();
    var startOfMonth = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), 1, 0, UPDATE_MINUTE, 0, 0);
    seconds = Math.floor((now - (startOfMonth))/1000);
    minutes = seconds/60;
    hours = Math.floor(minutes/60);

    if (localContains(NUMBER_OF_UPDATES_PROPERTY_NAME)){
        var previousNumber = localGet(NUMBER_OF_UPDATES_PROPERTY_NAME);

        if(previousNumber < hours){
            var diff = hours - previousNumber;
            var updateCountToast = document.getElementById("toast-update-count-text");

            if(diff == 1){
                updateCountToast.innerHTML = diff.toLocaleString() + " update";
            } else {
                updateCountToast.innerHTML = diff.toLocaleString() + " updates";
            }

            showToast("toast-update", true);
        }
    }

    localSet(NUMBER_OF_UPDATES_PROPERTY_NAME, hours);
}

// https://stackoverflow.com/questions/37179899/countdown-timer-every-hour-but-on-30-minute-marks
function updateTimer() {
    const zeroPad = (num, places) => String(num).padStart(places, '0')
    var time = new Date(),
    secsRemaining = 3600 - (time.getUTCMinutes()-UPDATE_MINUTE)%60 * 60 - time.getUTCSeconds();
    minutes = Math.floor(secsRemaining / 60) % 60;
    seconds = secsRemaining % 60;
    document.getElementById("min-part").innerHTML = minutes;
    document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2);

    if(minutes === 0 && seconds === 0){
        showToast("toast-refresh", false);
    }

    setTimeout(updateTimer, 1000 - (new Date()).getUTCMilliseconds());
}