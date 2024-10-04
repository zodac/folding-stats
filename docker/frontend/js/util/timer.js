const NUMBER_OF_UPDATES_PROPERTY_NAME = "numberOfUpdates"

const UPDATE_ENABLED = "%UPDATE_ENABLED%"
const FIRST_DAY_OF_MONTH = 3
const UPDATE_MINUTE = 55

function startTimer() {
    calculateNumberOfUpdates()
    updateTimer()
}

function calculateNumberOfUpdates() {
    now = new Date()

    startOfMonth = Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), FIRST_DAY_OF_MONTH, 0, UPDATE_MINUTE, 0, 0)
    seconds = Math.floor((now - (startOfMonth)) / MILLISECONDS_IN_SECOND)
    minutes = seconds / SECONDS_IN_MINUTE
    hours = Math.floor(minutes / MINUTES_IN_HOUR)

    if (localContains(NUMBER_OF_UPDATES_PROPERTY_NAME)) {
        previousNumber = localGet(NUMBER_OF_UPDATES_PROPERTY_NAME)

        if (previousNumber < hours) {
            diff = hours - previousNumber
            updateCountToast = document.getElementById("toast-update-count-text")
            updateCountToast.innerHTML = diff.toLocaleString() + (diff === 1 ? " update" : " updates")
            showToast("toast-update", true)
        }
    }

    localSet(NUMBER_OF_UPDATES_PROPERTY_NAME, hours)
}

// https://stackoverflow.com/questions/37179899/countdown-timer-every-hour-but-on-30-minute-marks
function updateTimer() {
    if (UPDATE_ENABLED == "false") {
        // Do not set up a timer if updating is disabled, and hide updates from the webpage
        hide("update_timer")
        return
    }

    const currentDayOfMonth = new Date().getUTCDate()
    const zeroPad = (num, places) => String(num).padStart(places, '0')

    // If within the update period, set the update value to the next UPDATE_MINUTE
    if (currentDayOfMonth >= parseInt(FIRST_DAY_OF_MONTH)) {
        let time = new Date()
        let secsRemaining = SECONDS_IN_HOUR - (time.getUTCMinutes() - UPDATE_MINUTE) % SECONDS_IN_MINUTE * SECONDS_IN_MINUTE - time.getUTCSeconds()
        let minutes = Math.floor(secsRemaining / SECONDS_IN_MINUTE) % SECONDS_IN_MINUTE
        let seconds = secsRemaining % SECONDS_IN_MINUTE

        document.getElementById("min-part").innerHTML = minutes
        document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2)

        if (minutes === 0 && seconds === 0) {
            showToast("toast-refresh", false)
        }
    } else { // If not within update period, set the update value to the FIRST_DAY_OF_MONTH
        let time = new Date()
        let msToStart = Math.abs(time - Date.UTC(time.getUTCFullYear(), time.getUTCMonth(), FIRST_DAY_OF_MONTH))
        let minutes = Math.floor(msToStart / MILLISECONDS_IN_MINUTE)
        let seconds = Math.floor((msToStart % MILLISECONDS_IN_MINUTE) / MILLISECONDS_IN_SECOND)

        document.getElementById("min-part").innerHTML = minutes
        document.getElementById("sec-part").innerHTML = zeroPad(seconds, 2)

        if (minutes === 0 && seconds === 0) {
            showToast("toast-refresh", false)
        }
    }

    // We want to check every 1s regardless of whether we're in the update period or not
    setTimeout(updateTimer, MILLISECONDS_IN_SECOND - (new Date()).getUTCMilliseconds())
}